(ns dd.srd.weapons)

(def simple-melee
  (map
    (partial merge {:category :simple :class :melee})
    [{:name "Club"
      :cost {:silver 1}
      :damage {:base :1d4 :type :bludgeoning}
      :weight 2
      :properties #{:light}}
     {:name "Dagger"
      :cost {:gold 2}
      :damage {:base :1d4 :type :piercing}
      :weight 1
      :range {:normal 20 :long 60}
      :properties #{:finesse :light :thrown}}
     {:name "Greatclub"
      :cost {:silver 2}
      :damage {:base :1d8 :type :bludgeoning}
      :weight 10
      :properties #{:two-handed}}
     {:name "Handaxe"
      :cost {:gold 5}
      :damage {:base :1d6 :type :slashing}
      :weight 2
      :range {:normal 20 :long 60}
      :properties #{:light :thrown}}
     {:name "Javelin"
      :cost {:silver 5}
      :damage {:base :1d6 :type :piercing}
      :weight 2
      :range {:normal 30 :long 120}
      :properties #{:thrown}}
     {:name "Light Hammer"
      :cost {:gold 2}
      :damage {:base :1d4 :type :bludgeoning}
      :weight 2
      :range {:normal 20 :long 60}
      :properties #{:light :thrown}}
     {:name "Mace"
      :cost {:gold 2}
      :damage {:base :1d4 :type :piercing}
      :weight 4}
     {:name "Quarterstaff"
      :cost {:silver 2}
      :damage {:base :1d4 :type :bludgeoning}
      :two-hand-damage :1d8
      :weight 4
      :properties #{:versatile}}
     {:name "Sickle"
      :cost {:gold 1}
      :damage {:base :1d4 :type :slashing}
      :weight 2
      :properties #{:light}}
     {:name "Spear"
      :cost {:gold 1}
      :damage {:base :1d6 :type :piercing}
      :two-hand-damage :1d8
      :weight 3
      :range {:normal 20 :long 60}
      :properties #{:thrown :versatile}}]))

(def martial-melee
  (map
    (partial merge {:category :martial :class :melee})
    [{:name "Battleaxe"
      :cost {:gold 10}
      :damage {:base :1d8 :type :slashing}
      :two-hand-damage :1d10
      :weight 4
      :properties #{:versatile}}
     {:name "Flail"
      :cost {:gold 10}
      :damage {:base :1d8 :type :bludgeoning}
      :weight 2}
     {:name "Glaive"
      :cost {:gold 20}
      :damage {:base :1d10 :type :slashing}
      :weight 6
      :properties #{:heavy :reach :two-handed}}
     {:name "Greataxe"
      :cost {:gold 30}
      :damage {:base :1d12 :type :slashing}
      :weight 7
      :properties #{:heavy :two-handed}}
     {:name "Greatsword"
      :cost {:gold 50}
      :damage {:base :2d6 :type :slashing}
      :weight 6
      :properties #{:heavy :two-handed}}
     {:name "Halberd"
      :cost {:gold 20}
      :damage {:base :1d10 :type :slashing}
      :weight 6
      :properties #{:heavy :reach :two-handed}}
     {:name "Lance"
      :cost {:gold 10}
      :damage {:base :1d12 :type :piercing}
      :weight 6
      :properties #{:reach :special}}
     {:name "Longsword"
      :cost {:gold 15}
      :damage {:base :1d8 :type :slashing}
      :two-hand-damage :1d10
      :weight 3
      :properties #{:versatile}}
     {:name "Maul"
      :cost {:gold 10}
      :damage {:base :2d6 :type :bludgeoning}
      :weight 10
      :properties #{:heavy :two-handed}}
     {:name "Morningstar"
      :cost {:gold 15}
      :damage {:base :1d8 :type :piercing}
      :weight 4}
     {:name "Pike"
      :cost {:gold 5}
      :damage {:base :1d10 :type :piercing}
      :weight 18
      :properties #{:heavy :reach :two-handed}}
     {:name "Rapier"
      :cost {:gold 25}
      :damage {:base :1d8 :type :piercing}
      :weight 2 
      :properties #{:finesse}}
     {:name "Scimitar"
      :cost {:gold 25}
      :damage {:base :1d6 :type :slashing}
      :weight 3 
      :properties #{:finesse :light}}
     {:name "Shortsword"
      :cost {:gold 10}
      :damage {:1d6 :piercing}
      :weight 2
      :properties #{:finesse :light}}
     {:name "Trident"
      :cost {:gold 5}
      :damage {:base :1d6 :type :piercing}
      :two-hand-damage :1d8
      :weight 4
      :range {:normal 20 :long 60}
      :properties #{:thrown :versatile}}
     {:name "War Pick"
      :cost {:gold 5}
      :damage {:base :1d8 :type :piercing}
      :weight 2}
     {:name "Warhammer"
      :cost {:gold 2}
      :damage {:base :1d8 :type :bludgeoning}
      :two-hand-damage :1d10
      :weight 2
      :properties #{:versatile}}
     {:name "Whip"
      :cost {:gold 2}
      :damage {:base :1d4 :type :slashing}
      :weight 3
      :properties #{:finesse :reach}}]))

(def simple-ranged
  (map
    (partial merge {:category :simple :class :ranged})
    [{:name "Crossbow, light"
      :cost {:gold 25}
      :damage {:base :1d8 :type :piercing}
      :weight 5
      :range {:normal 80 :long 320}
      :properties #{:ammunition :loading :two-handed}}
     {:name "Dart"
      :cost {:copper 5}
      :damage {:base :1d4 :type :piercing}
      :weight 0.25
      :range {:normal 20 :long 60}
      :properties #{:finesse :thrown}}
     {:name "Shortbow"
      :cost {:gold 25}
      :damage {:base :1d8 :type :piercing}
      :weight 2
      :range {:normal 80 :long 320}
      :properties #{:ammunition :two-handed}}
     {:name "Sling"
      :cost {:silver 1}
      :damage {:base :1d4 :type :bludgeoning}
      :range {:normal 30 :long 120}
      :properties #{:ammunition}}]))

(def martial-ranged
  (map
    (partial merge {:category :martial :class :ranged})
    [{:name "Blowgun"
      :cost {:gold 10}
      :damage {:base :1 :type :piercing}
      :weight 1
      :range {:normal 25 :long 100}
      :properties #{:ammunition :loading}}
     {:name "Crossbow, hand"
      :cost {:gold 75}
      :damage {:base :1d6 :type :piercing}
      :weight 3
      :range {:normal 30 :long 120}
      :properties #{:light :loading}}
     {:name "Crossbow, heavy"
      :cost {:gold 50}
      :damage {:base :1d10 :type :piercing}
      :weight 18
      :range {:normal 100 :long 400}
      :properties #{:ammunition :heavy :loading :two-handed}}
     {:name "Longbow"
      :cost {:gold 50}
      :damage {:base :1d8 :type :piercing}
      :weight 2
      :range {:normal 150 :long 600}
      :properties #{:ammunition :heavy :two-handed}}
     {:name "Net"
      :cost {:gold 1}
      :weight 3
      :range {:normal 5 :long 15}
      :properties #{:special :thrown}}]))

(def definitions
  (concat simple-melee
          simple-ranged
          martial-melee
          martial-ranged))

(def by-name
  (reduce
    (fn [m weapon]
      (assoc m (:name weapon) weapon))
    {}
    definitions))
