(ns sslt.seaver
  (:require [sslt.selector :as s]))

(defn select-methods
  [sw path-methods]
  (update sw "paths"
          (fn [paths]
            (reduce-kv (fn [acc path methods]
                         (if-let [path-data (paths path)]
                           (assoc acc path
                                  (reduce (fn [a method]
                                            (if-let [data (path-data (name method))]
                                              (assoc a (name method) data)
                                              (throw (ex-info (str "Undefined method")
                                                              {:path path
                                                               :method method}))))
                                          (sorted-map)
                                          methods))
                           (throw (ex-info (str "Undefined path")
                                           {:path path
                                            :methods methods}))))
                       (sorted-map)
                       path-methods))))

(defn select-types
  [sw types]
  (let [cut-type-names (map (comp last s/split-component-path) types)]
    (-> sw
        (update "components"
                (fn [components]
                  (dissoc components "securitySchemes")))
        (update-in ["components" "schemas"]
                   (fn [schema-types]
                     (reduce (fn [acc type-name]
                               (if-let [type-data (schema-types type-name)]
                                 (assoc acc type-name type-data)
                                 (throw (ex-info (str "Undefined type")
                                                 {:type type-name}))))
                             {}
                             cut-type-names))))))

(defn seave-methods
  [sw path-methods]
  (let [types (s/collect-types sw path-methods)]
    (-> sw
        (select-methods path-methods)
        (select-types types)
        (dissoc "info" "tags" "servers" "security"))))
