(ns cartographer.core
  (:require [cartographer.reader :as reader]
            [cartographer.viewer :as viewer])
  (:gen-class :main true))

(set! *warn-on-reflection* true)

(defprotocol BlockAccessor
  (block [this x y z])
  (lazy-seq-with-coordinates [this]))

(defrecord Blocks [#^bytes bytes]
  BlockAccessor
  (block [this x y z] (aget bytes (+ y (* z 128) (* x 128 16))))
  (lazy-seq-with-coordinates [this]
    (map #(assoc % :block-id %2) (for [x (range (* 128 16)) z (range 128) y (range 16)] {:x x :y y :z z}) bytes)
    )
  )
      
(defn -main []
  (def save_folder "/home/roman/.minecraft/saves/Genesis")
  (def region_files (cartographer.reader/region_files save_folder))
  (prn region_files)
  (def chunks (cartographer.reader/load-region-file ((first (cartographer.reader/region_files save_folder)) :file )))
  (comment (for [chunk (take 10 chunks)]
             (do (prn "==========")
                 (prn chunk))))
  (def first-blocks (((last ((first chunks) :data)) "Level" ) "Blocks" ))
  (def blocks (Blocks. first-blocks))

  
  )

