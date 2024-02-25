(ns sslt.core
  (:require [cheshire.core :refer [parse-string generate-string]]
            [sslt.seaver :as s]))

(defn- get-in-path
  [filename]
  (str "resources/swagger/" filename ".json"))

(defn- get-out-path
  [filename]
  (str "out/swagger/" filename ".json"))

(defn read-file
  [filename]
  (parse-string (slurp (get-in-path filename))))

(defn write-file
  [filename sw]
  (let [out-path (get-out-path filename)]
    (spit out-path (generate-string sw {:pretty false}))
    (str filename " has successfully written")))

(defn process-file
  [filename path-methods]
  (try
    (let [in-sw (read-file filename)
          out-sw (s/seave-methods in-sw path-methods)]
      (write-file filename out-sw))
    (catch clojure.lang.ExceptionInfo e
      {:type (type e)
       :cause (.getMessage e)
       :data (ex-data e)})))

(defn get-methods
  [sw]
  (reduce-kv (fn [acc path methods]
               (assoc acc path (mapv keyword (keys methods))))
             (sorted-map)
             (get sw "paths")))

(defn read-methods
  [filename]
  (get-methods (read-file filename)))

(comment

  )
