(ns cartographer.core
  (:require cartographer.reader)
  (:gen-class :main true))

(defn -main []
  (def save_folder "/home/roman/.minecraft/saves/Genesis")
  (def region_files (cartographer.reader/region_files save_folder))
  (prn region_files)
  (def chunks (cartographer.reader/load-region-file ((first (cartographer.reader/region_files save_folder)) :file )))
  (prn (first chunks))
  )

