(ns cartographer.reader)

(import '(java.io File))
(import '(java.nio.file Files))

(defn bytes-to-int [bytes]
  (let [b (if (> 4 (count bytes)) (concat (repeat (- 4 ( count bytes)) 0) bytes) bytes)]
    (.getInt (java.nio.ByteBuffer/wrap (into-array Byte/TYPE b)))))

(defn region_files [save_folder]
  (map
   #(merge {:file %}
           (apply hash-map (interleave
                 [:x :y]
                 (map (fn [x] (Integer/parseInt x)) (drop 1 (re-find #"r.(-?\d+).(-?\d+).mcr" (.getName %)))))))
   (seq (.listFiles (File. (File. save_folder) "region")))))

(defn resolve-tag [stream & {:keys [named? type] :or {named? true}}]
  (case (or type (.read stream))
    10 [(if named? (.readUTF stream) nil)
           (loop [acc []]
             (let [tag (resolve-tag stream)]
               (if (= tag "TAG_End")
                 (apply hash-map (reduce concat acc))
                 (recur (conj acc tag)))))] ;TAG_COMPOUND
    9 [(if named? (.readUTF stream) nil)
       (let [list-type (.readByte stream)]
         (doall (take (.readInt stream) (repeatedly #(resolve-tag stream :named? false :type list-type)))))] ;TAG_List
    8 [(if named? (.readUTF stream) nil) (.readUTF stream)] ;TAG_String
    7 [(if named? (.readUTF stream) nil)
       (let [bytes (byte-array (.readInt stream))]
         (.readFully stream bytes)
         bytes)] ;TAG_Byte_Array
    6 [(if named? (.readUTF stream) nil) (.readDouble stream)] ;TAG_Double
    5 [(if named? (.readUTF stream) nil) (.readFloat stream)] ;TAG_Float
    4 [(if named? (.readUTF stream) nil) (.readLong stream)] ;TAG_Long
    3 [(if named? (.readUTF stream) nil) (.readInt stream)] ;TAG_Int
    2 [(if named? (.readUTF stream) nil) (.readShort stream)] ;TAG_Short
    1 [(if named? (.readUTF stream) nil) (.readByte stream)] ;TAG_Byte
    0 "TAG_End" ;TAG_END
    ))

(defn decompress-chunk-data [bytes compression]
  {:pre [(some #{1 2} [compression])]}
  ( let [byte_input_stream (java.io.ByteArrayInputStream. (into-array Byte/TYPE bytes))]
    (java.io.DataInputStream. (if (= 1 compression) (java.util.zip.GZIPInputStream. byte_input_stream) (java.util.zip.InflaterInputStream. byte_input_stream)))))

(defn decode-chunk-data [data]
  (let [
        length (dec (bytes-to-int (take 4 data)))
        compression_type (nth data 4)
        compressed_data (drop 5 data)
        ]
    (resolve-tag (decompress-chunk-data (take length compressed_data) compression_type))))

(defn load-region-file [region_file]
  (let [
        region_bytes (java.nio.file.Files/readAllBytes (.toPath region_file))
        chunk_locations (take 4096 region_bytes)
        chunk_offsets (map #(- (bytes-to-int %) 2) (partition 3 4 chunk_locations))
        chunk_sector_counts (map bytes-to-int (partition 1 4 (drop 3 chunk_locations)))
        chunk_timestamps (map bytes-to-int (partition 4 (take 4096 (drop 4096 region_bytes))))
        chunk_data (vec (partition 4096 (drop 8192 region_bytes)))
        coordinates (for [z (range 32) x (range 32)] [x z])
        chunks (map
                #(identity {:x (first %4)
                            :z (last %4)
                            :offset %1
                            :sector_count %2
                            :timestamp %3
                            :data (if (> %2 0)
                                    (decode-chunk-data (vec (flatten (subvec chunk_data %1 (+ %1 %2)))))
                                    [] )})
                chunk_offsets
                chunk_sector_counts
                chunk_timestamps
                coordinates)
        ]
    (remove #(= 0 (% :sector_count)) chunks)
    ))



(comment
  (def saves_folder (clojure.java.io/file "/home/roman/.minecraft/saves"))
  (def saves (map #(.getName %) (seq (.listFiles saves_folder))))

  (println "Save files:")
  (doseq [file saves] (println file))
  (def region_folder (clojure.java.io/file "/home/roman/.minecraft/saves/Genesis/region/"))
  (def region_files (map #(vec [% (re-find #"r.(-?\d+).(-?\d+).mcr" (.getName %))]) (seq (.listFiles region_folder))))

  (println "Regions:")
  (doseq [file region_files] (println file))


)