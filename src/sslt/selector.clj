(ns sslt.selector
  (:require [clojure.string :as s]
            [clojure.set :as o]))

(defn get-schema-inner-type
  [schema]
  (or (get schema "$ref")
      (case (get schema "type")
        "array" (get-in schema ["items" "$ref"])
        "object" (get-schema-inner-type (get schema "additionalProperties"))
        nil)))

(defn get-schema-inner-types
  [object]
  (->> (vals (object "properties"))
       (map get-schema-inner-type)
       (remove nil?)))

(defn get-schema-type
  [item]
  (get-schema-inner-type (get item "schema")))

(defn collect-param-types
  [method-data]
  (->> (get method-data "parameters")
       (map get-schema-type)
       (remove nil?)))

(defn collect-request-types
  [method-data]
  (->> (get-in method-data ["requestBody" "content"])
       vals
       (map get-schema-type)
       (remove nil?)))

(defn collect-response-types
  [method-data]
  (->> (get method-data "responses")
       vals
       (map #(get % "content"))
       (mapcat vals)
       (map get-schema-type)
       (remove nil?)))

(defn split-component-path
  [component-path]
  (rest (s/split component-path #"/")))

(defn collect-method-types
  [sw path methods]
  (->> (map #(get-in sw ["paths" path (name %)]) methods)
       (mapcat (fn [method-data]
                 (concat (collect-param-types method-data)
                         (collect-request-types method-data)
                         (collect-response-types method-data))))
       set))

(defn collect-sub-types
  ([sw types]
   (collect-sub-types sw types #{}))
  ([sw types except]
   (if (empty? types)
     #{}
     (let [sub-types (->> (map split-component-path types)
                          (map (partial get-in sw))
                          (mapcat get-schema-inner-types)
                          set)
           prev (o/union (set types) except)
           all (o/union prev sub-types)
           diff (o/difference sub-types prev)]
       (set (concat sub-types
                    (collect-sub-types sw diff all)))))))

(defn collect-types
  [sw path-methods]
  (let [method-types (set
                      (mapcat (fn [[path methods]]
                                (collect-method-types sw path methods)) path-methods))
        sub-types (collect-sub-types sw method-types)]
    (into (sorted-set)
          (o/union method-types sub-types))))

(comment

  (split-component-path "#/components/schemas/LoanPetitionDto")

  )
