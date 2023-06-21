(ns sslt.selector-test
  (:require [clojure.test :refer :all]
            [sslt.selector :refer :all]
            [sslt.core :as core]))

(def lp-sw
  (core/read-file "vkkp/loan-petitions-api-doc"))

(def paths
  (get lp-sw "paths"))

(def scenario-path "/scenario/{id}")
(def lp-path "/loan-petition/{id}")

(defn- get-mtd
  [path method]
  (get-in paths [path (name method)]))

(deftest collect-param-types-test
  (testing "Empty params"
    (is (empty? (collect-param-types (get-mtd scenario-path :put))))))

(deftest collect-request-types-test
  (testing "ScenarioDto from request"
    (is (= ["#/components/schemas/ScenarioDto"]
           (collect-request-types (get-mtd scenario-path :put)))))
  (testing "LoanPetitionDtoRequest from request"
    (is (= ["#/components/schemas/LoanPetitionDtoRequest"]
           (collect-request-types (get-mtd lp-path :put)))))
  (testing "LoanPetitionDtoRequest from request"
    (is (empty? (collect-request-types (get-mtd lp-path :get))))))

(deftest collect-response-types-test
  (testing "ScenarioDto from response"
    (is (= ["#/components/schemas/ScenarioDto"]
           (collect-response-types (get-mtd scenario-path :put)))))
  (testing "LoanPetitionDtoresponse from response"
    (is (= ["#/components/schemas/LoanPetitionDto"]
           (collect-response-types (get-mtd lp-path :put)))))
  (testing "LoanPetitionDto from response"
    (is (= ["#/components/schemas/LoanPetitionDto"]
           (collect-response-types (get-mtd lp-path :get))))))

(deftest collect-method-types-test
  (testing "ScenarioDto from response"
    (is (= #{"#/components/schemas/ScenarioDto"}
           (collect-method-types lp-sw scenario-path [:put]))))
  (testing "LoanPetitionDtoresponse from response"
    (is (= #{"#/components/schemas/LoanPetitionDto"
             "#/components/schemas/LoanPetitionDtoRequest"}
           (collect-method-types lp-sw lp-path [:get :put])))))

(deftest collect-sub-types-test
  (testing "ForbiddenPurposeDto from type"
    (is (= #{"#/components/schemas/ForbiddenPurposeDto"}
           (collect-sub-types lp-sw #{"#/components/schemas/ControlsSettingsDto"}))))
  (testing "ControlsSettingsDto from type"
    (is (= #{"#/components/schemas/ControlsSettingsDto"
             "#/components/schemas/ForbiddenPurposeDto"}
           (collect-sub-types lp-sw #{"#/components/schemas/ScenarioDto"}))))
  (testing "Few types from types"
    (is (= #{"#/components/schemas/ForbiddenPurposeDto"
             "#/components/schemas/LegalEntityDto"
             "#/components/schemas/LoanPetitionStatus"
             "#/components/schemas/ControlsSettingsDto"}
           (collect-sub-types lp-sw #{"#/components/schemas/ControlsSettingsDto"
                                      "#/components/schemas/LoanPetitionDto"
                                      "#/components/schemas/ScenarioDto"})))))

(deftest collect-types-test
  (testing "All types for a few methods"
    (is (= #{"#/components/schemas/ControlsSettingsDto"
             "#/components/schemas/ForbiddenPurposeDto"
             "#/components/schemas/LegalEntityDto"
             "#/components/schemas/LoanPetitionDto"
             "#/components/schemas/LoanPetitionDtoRequest"
             "#/components/schemas/LoanPetitionStatus"
             "#/components/schemas/ScenarioDto"}
           (collect-types lp-sw {scenario-path [:put]
                                 lp-path [:get :put]})))))
