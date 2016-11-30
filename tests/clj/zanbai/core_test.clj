(ns zanbai.core_test
  (:use [zanbai.core])
  (:use [clojure.test])
)

(deftest silly
  (is (= 0 (- 1 1))))
