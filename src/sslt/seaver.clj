(ns sslt.seaver
  (:require [sslt.selector :as s]))

(defn- methods-builder
  [path path-data acc method]
  (if-let [data (path-data (name method))]
    (assoc acc (name method) data)
    (throw (ex-info (str "Undefined method")
                    {:path path
                     :method method}))))

(defn- paths-builder
  [paths acc path methods]
  (if-let [path-data (paths path)]
    (assoc acc path
           (reduce (partial methods-builder path path-data)
                   (sorted-map)
                   methods))
    (throw (ex-info (str "Undefined path")
                    {:path path
                     :methods methods}))))

(defn- update-paths
  [paths path-methods]
  (reduce-kv (partial paths-builder paths)
             (sorted-map)
             path-methods))

(defn- select-methods
  [sw path-methods]
  (update sw "paths" update-paths path-methods))

(defn- build-type
  [schemas acc type-name]
  (if-let [type-data (schemas type-name)]
    (assoc acc type-name type-data)
    (throw (ex-info (str "Undefined type")
                    {:type type-name}))))

(defn- update-schemas
  [schemas cut-type-names]
  (reduce (partial build-type schemas)
          {}
          cut-type-names))

(defn- select-types
  [sw types]
  (let [cut-type-names (map (comp last s/split-component-path) types)]
    (-> sw
        ;; (update "components" dissoc "securitySchemes")
        (update-in ["components" "schemas"] update-schemas cut-type-names))))

(defn seave-methods
  [sw path-methods]
  (let [types (s/collect-types sw path-methods)]
    (-> sw
        (select-methods path-methods)
        (select-types types)
        ;; (dissoc "info" "tags" "servers" "security")
        )))
