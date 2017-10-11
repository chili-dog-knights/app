(ns dd.srd.armor)

(def light
  (map
    (partial merge {:category :light :armor-class-ability-modifiers #{:dexterity} :don "1 minute" :doff "1 minute"})
    [{:name "Padded"
      :cost {:gold 5}
      :armor-class 11
      :stealth :disadvantage
      :weight 8}
     {:name "Leather"
      :cost {:gold 10}
      :armor-class 11
      :weight 10}
     {:name "Studded Leather"
      :cost {:silver 45}
      :armor-class 12
      :weight 13}]))

(def medium
  (map
    (partial merge {:category :medium 
                    :armor-class-ability-modifiers #{:dexterity}
                    :armor-class-ability-modifier-limit 2
                    :don "5 minutes"
                    :doff "1 minute"})
    [{:name "Hide"
      :cost {:gold 10}
      :armor-class 12
      :weight 12}
     {:name "Chain Shirt"
      :cost {:gold 50}
      :armor-class 13
      :weight 20}
     {:name "Scale Mail"
      :cost {:gold 50}
      :armor-class 14
      :stealth :disadvantage
      :weight 45}
     {:name "Breastplate"
      :cost {:gold 400}
      :armor-class 14
      :weight 20}
     {:name "Half Plate"
      :cost {:gold 750}
      :armor-class 15
      :stealth :disadvantage
      :weight 40}]))

(def heavy
  (map
    (partial merge {:category :heavy :don "10 minutes" :doff "5 minutes"})
    [{:name "Ring Mail"
      :cost {:gold 30}
      :armor-class 14
      :stealth :disadvantage
      :weight 40}
     {:name "Chain Mail"
      :cost {:gold 75}
      :armor-class 16
      :ability-requirement {:stealth 13}
      :stealth :disadvantage
      :weight 55}
     {:name "Splint"
      :cost {:gold 200}
      :armor-class 17
      :ability-requirement {:stealth 15}
      :stealth :disadvantage
      :weight 60}
     {:name "Plate"
      :cost {:gold 1500}
      :armor-class 18
      :ability-requirement {:stealth 15}
      :stealth :disadvantage
      :weight 65}]))

(def shield
  (map
    (partial merge {:category :shield :don "1 action" :doff "1 action"})
    [{:name "Shield"
      :cost {:gold 10}
      :armor-class 2
      :weight 6}]))

(def definitions
  (concat light
          medium
          heavy
          shield))

(def by-name
  (reduce
    (fn [m armor]
      (assoc m (:name armor) armor))
    {}
    definitions))
