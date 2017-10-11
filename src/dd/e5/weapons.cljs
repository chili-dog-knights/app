(ns dd.e5.weapons)

(def martial-melee
  (map
    (partial merge {:category :martial :class :melee})
    [{:name "Katana"
      :cost {:gold 15}
      :damage {:base :1d8 :type :slashing}
      :two-hand-damage :1d10
      :weight 3 
      :properties #{:special :versatile}}]))

(def definitions
  (concat martial-melee))

(def by-name
  (reduce
    (fn [m weapon]
      (assoc m (:name weapon) weapon))
    {}
    definitions))
