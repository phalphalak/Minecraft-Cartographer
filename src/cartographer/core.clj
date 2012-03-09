(ns cartographer.core
  (:require cartographer.reader)
  (:gen-class :main true))

(defn -main []
  (def save_folder "/home/roman/.minecraft/saves/Genesis")
  (prn save_folder)
  (prn (cartographer.reader/region_files save_folder))
  (def chunks (cartographer.reader/load-region-file ((first (cartographer.reader/region_files save_folder)) :file )))
  (prn (count  chunks))
  )

