(ns zanbai.core_test
  (:use [zanbai.core])
  (:use [clojure.test])
)

(deftest test-get-pending-messages
  (is (= nil (get-pending-messages 'adam')))
  )
