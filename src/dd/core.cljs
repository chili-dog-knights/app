(ns dd.core
  (:require 
    [accountant.core :as accountant]
    [alandipert.storage-atom :refer [local-storage]]
    [clojure.contrib.humanize :as humanize]
    [clojure.string :as str]
    [compassus.core :as compassus]
    [dd.e5.weapons :as e5-weapons]
    [dd.srd.armor :as srd-armor]
    [dd.srd.spells :as srd-spells]
    [dd.srd.weapons :as srd-weapons]
    [dd.search :as search]
    [goog.dom :as gdom]
    [markdown.core :as md]
    [om.next :as om :refer-macros [defui ui]]
    [om.dom :as dom]
    [secretary.core :as secretary :refer-macros [defroute]]))

(enable-console-print!)

;; define your app data so that it doesn't get over-written on reload
(defonce app-state
  (atom {:skills {:acrobatics :dexterity
                  :animal-handling :wisdom
                  :arcana :intelligence
                  :athletics :strength
                  :deception :charisma
                  :history :intelligence
                  :insight :wisdom
                  :intimidation :charisma
                  :investigation :intelligence
                  :medicine :wisdom
                  :nature :intelligence
                  :perception :wisdom
                  :performance :charisma
                  :persuasion :charisma
                  :religion :intelligence
                  :sleight-of-hand :dexterity
                  :stealth :dexterity
                  :survival :wisdom}
         :backgrounds {:hermit {:money {:gold 5}
                                :skill-proficiencies #{:medicine :religion}
                                :tool-proficiences #{:herbalism-kit}
                                :bonds ["Nothing is more imporant than the other members of my hermitage, order, or association."
                                        "I entered seculsion to hide from the ones who might sill be hunting me. I must someday confront them."
                                        "I am still seeking the enlightenment I pursued in my seclusion, and it still eludes me."
                                        "I entered seclusion because I loved someone I could nto have."
                                        "Should my discovery come to light, it could bring ruin to the world."
                                        "My isolation gave me great insight into a great evil that only I can destroy."]
                                :flaws ["Now that I've returned to the world, I enjoy its delights a little too much."
                                        "I harbor dark, bloodthirsty thoughts that my isolation and mediation failed to quell."
                                        "I am dogmatic in my thoughts and philosophy."
                                        "I let my need to win arguments overshadow friendships and harmony."
                                        "I'd risk too much to uncover a lost bit of knowledge"
                                        "I like keeping secrets and won't share them with anyone."]
                                :ideals [{:alignment :good :name "Greater Good" :description "My gifts are meant to be shared with all, not used for my own benefit"}
                                         {:alignment :lawful :name "Logic" :description "Emotions must not cloud our sense of what is right and true, or our logical thinking."}
                                         {:alignment :chaotic :name "Free Thinking" :description "Inquiry and curiosity are the pillars of progress."}
                                         {:alignment :evil :name "Power" :description "Solitude and contemplation are paths toward mystical or magical power."}
                                         {:alignment :neutral :name "Live and Let Live" :description "Meddling in the affairs of others only causes trouble."}
                                         {:name "Self-Knowledge" :description "If you know yourself, there's nothing left to know."}]
                                :traits ["I've been isolated for so long that I rarely speak, preferring gestures and the occasional grunt."
                                         "I am utterly serene, even in the face of disaster."
                                         "The leader of my community had something wise to say on every topic, and I am eager to share that wisdom."
                                         "I feel tremendous empathy for all who suffer."
                                         "I am oblivious to etiquette and social expectations."
                                         "I connect everything that happens to me to a grand, cosmic plan."
                                         "I often get lost in my own thoughts and contemplation, becoming oblivious to my surroundings."
                                         "I am working on a grand philosophical theory and love sharing my ideas."]}} 
         :armor/by-name srd-armor/by-name
         :weapons/by-name (merge srd-weapons/by-name e5-weapons/by-name)
         :spells/by-id srd-spells/by-id
         :classes {:fighter {:hit-dice :1d10
                            :proficiencies {:armor #{:light :medium :heavy :shields}
                                            :weapons #{:simple :martial}}
                            :saving-throws #{:strength :constitution}
                            :scale [{:features #{:fighting-style :second-wind}}
                                    {:features #{:action-surge}}
                                    {:features #{:martial-archetype}}
                                    {:ability-score-improvement 1}
                                    {:features #{:extra-attack}}]}
                   :ranger {:hit-dice :1d10
                            :proficiencies {:armor #{:light :medium :shields}
                                            :weapons #{:simple :martial}}
                            :saving-throws #{:strength :dexterity}
                            :scale [{:features #{:favored-enemy :natural-explorer}}
                                    {:known-spells 2
                                     :features #{:fighting-style :spellcasting}
                                     :spell-slots 2}
                                    {:known-spells 1
                                     :features #{:archetype :primeval-awareness}
                                     :spell-slots 1}
                                    {:ability-score-improvement 1}
                                    {:known-spells 1
                                     :features #{:extra-attack}
                                     :spell-slots 1}]}
                   :warlock {:hit-dice :1d8
                             :proficiencies {:armor #{:light}
                                             :weapons #{:simple}}
                             :saving-throws #{:wisdom :charisma}
                             :scale [{:known-cantrips    2
                                      :known-spells      2
                                      :features #{:otherworldly-patron :pact-magic}
                                      :slot-level        1
                                      :spell-slots       1}
                                     {:known-invocations 2
                                      :known-spells      1
                                      :features #{:eldritch-invocations}
                                      :spell-slots       1}
                                     {:known-spells      1
                                      :features #{:pact-boon}
                                      :slot-level        1}
                                     {:ability-score-improvement true
                                      :known-cantrips    1
                                      :known-spells      1}
                                     {:known-invocations 1
                                      :known-spells      1
                                      :slot-level        1}]}
                   :rogue {:hit-dice :1d8
                           :proficiencies {:armor #{:light}
                                           :weapons #{:simple :martial}}
                           :saving-throws #{:dexterity :intelligence}
                           :scale [{:features #{:favored-enemy :natural-explorer}}
                                   {:known-spells 2
                                    :features #{:fighting-style :spellcasting}
                                    :spell-slots 2}
                                   {:known-spells 1
                                    :features #{:archetype :primeval-awareness}
                                    :spell-slots 1}
                                   {:ability-score-improvement 1}
                                   {:known-spells 1
                                    :features #{:extra-attack}
                                    :spell-slots 1}]}}
         :races {:arakocra {:ability-score-increase {:dexterity 2
                                                     :wisdom 1}
                            :base-speed 25
                            :languages #{:common :arakocra}}
                 :dragonborn {:ability-score-increase {:strength 2
                                                       :charisma 1}
                              :base-speed 30
                              :languages #{:common :draconic}}
                 :dwarf      {:ability-score-increase {:constitution 2}
                              :base-speed 25
                              :languages #{:common :dwarvish}}
                 :hill-dwarf {:ability-score-increase {:wisdom 1}
                              :base-speed 25
                              :languages #{:common :dwarvish}}
                 :elf        {:ability-score-increase {:dexterity 2}
                              :base-speed 30
                              :languages #{:common :elvish}}
                 :half-elf   {:ability-score-increase {:charisma 2}
                              :base-speed 30
                              :languages #{:common :elvish}}
                 :gnome      {:ability-score-increase {:intelligence 2}
                              :base-speed 25
                              :languages #{:common :gnomish}}
                 :half-orc   {:ability-score-increase {:strength 2
                                                       :constitution 1}
                              :base-speed 30
                              :languages #{:common :orc}}
                 :halfling   {:ability-score-increase {:dexterity 2}
                              :base-speed 25
                              :languages #{:common :halfling}}
                 :human      {:ability-score-increase {:strength 1
                                                       :dexterity 1
                                                       :constitution 1
                                                       :intelligence 1
                                                       :wisdom 1
                                                       :charisma 1}
                              :base-speed 30
                              :languages #{:common}}
                 :tiefling   {:ability-score-increase {:intelligence 1
                                                       :charisma 2}
                              :base-speed 30
                              :languages #{:common :infernal}}}
         :levels [{:level 1  :experience 0      :proficiency-bonus 2}
                  {:level 2  :experience 300    :proficiency-bonus 2}
                  {:level 3  :experience 900    :proficiency-bonus 2}
                  {:level 4  :experience 2700   :proficiency-bonus 2}
                  {:level 5  :experience 6500   :proficiency-bonus 3}
                  {:level 6  :experience 14000  :proficiency-bonus 3}
                  {:level 7  :experience 23000  :proficiency-bonus 3}
                  {:level 8  :experience 34000  :proficiency-bonus 3}
                  {:level 9  :experience 48000  :proficiency-bonus 4}
                  {:level 10 :experience 64000  :proficiency-bonus 4}
                  {:level 11 :experience 85000  :proficiency-bonus 4}
                  {:level 12 :experience 100000 :proficiency-bonus 4}
                  {:level 13 :experience 120000 :proficiency-bonus 5}
                  {:level 14 :experience 140000 :proficiency-bonus 5}
                  {:level 15 :experience 165000 :proficiency-bonus 5}
                  {:level 16 :experience 195000 :proficiency-bonus 5}
                  {:level 17 :experience 225000 :proficiency-bonus 6}
                  {:level 18 :experience 265000 :proficiency-bonus 6}
                  {:level 19 :experience 305000 :proficiency-bonus 6}
                  {:level 20 :experience 355000 :proficiency-bonus 6}]
         :characters [{:id "056ef4fc-6e87-40fb-90b9-9e185ad466c2"
                       :alignment :lawful-good
                       :abilities {:strength     13
                                   :dexterity    11
                                   :constitution 15
                                   :intelligence 11
                                   :wisdom       13
                                   :charisma     16}
                       :background :hermit #_ {:type :hermit
                                    :bond 1
                                    :flaw 1
                                    :ideal 1
                                    :traits #{}}
                       :classes {:warlock {:experience 2345}}
                       :hit-points {:maximum 20 :current 16}
                       :money {:platinum 0
                               :gold 53
                               :electrum 0
                               :silver 395
                               :copper 700}
                       :name "Dahkmag"
                       :race :tiefling
                       :skill-proficiences #{:deception :investigation :medicine :religion}
                       :spellcasting-ability :charisma
                       :spells [[:spells/by-id "c113c453-ea97-49c4-864a-608cbdf5237f"]
                                [:spells/by-id "df783fb4-1c14-4825-a3e4-b97232f35863"]
                                [:spells/by-id "f6194e15-1cbe-4951-945c-100a0eb18b43"]
                                [:spells/by-id "81925932-e62f-46a0-81b0-adc5fcc2a8e8"]
                                [:spells/by-id "d07bb5be-6c48-4b14-9eba-27e6ccd66f09"]
                                [:spells/by-id "ab547a94-a8f7-42d5-8e19-83a22ac87422"]
                                [:spells/by-id "6423857a-4a06-4840-beed-b6a7e8b0cdaa"]
                                [:spells/by-id "0d4da5b9-8ed1-4931-b1d2-668bef927ef9"]
                                [:spells/by-id "8c3a7e2c-695b-456c-9942-b4b883e23cac"]]
                       :armor [[:armor/by-name "Leather"]]
                       :equipment [[:weapons/by-name "Dagger"]
                                   [:weapons/by-name "Dagger"]
                                   [:weapons/by-name "Quarterstaff"]]}
                      {:id "90c47836-eab7-4401-99b4-070a5b0b4007"
                       :alignment :lawful-good
                       :abilities {:strength     17
                                   :dexterity    15
                                   :constitution 18
                                   :intelligence 14
                                   :wisdom       14
                                   :charisma     13}
                       :background :soldier
                       :classes {:fighter {:experience 1525}}
                       :hit-points {:maximum 30 :current 30}
                       :money {:platinum 0
                               :gold 13
                               :electrum 0
                               :silver 200
                               :copper 0}
                       :name "Ashleigh Thunderpaw"
                       :race :hill-dwarf
                       :skill-proficiences #{:athletics :perception :survival :intimidation}
                       :armor [[:armor/by-name "Leather"]]
                       :equipment [[:weapons/by-name "Longbow"]
                                   [:weapons/by-name "Greataxe"]
                                   [:weapons/by-name "Warhammer"]]}
                      {:id "a3d0f6d7-d02b-4922-9878-c3f113a67f24"
                       :alignment :lawful-good
                       :abilities {:strength     14
                                   :dexterity    14
                                   :constitution 14
                                   :intelligence 14
                                   :wisdom       13
                                   :charisma     12}
                       :background :sage
                       :classes {:ranger {:experience 4960}}
                       :hit-points {:maximum 85 :current 50}
                       :money {:platinum 1130
                               :gold 3608
                               :electrum 0
                               :silver 120
                               :copper 0}
                       :name "Jakeolas"
                       :race :half-elf
                       :skill-proficiences #{:animal-handling :arcana :history :investigation :medicine :stealth :survival}
                       :spellcasting-ability :wisdom
                       :spells [[:spells/by-id "27647da2-30db-4d3e-a4fd-840a607fb635"]
                                [:spells/by-id "a28f38bf-9ccd-44d7-b489-cd071814ea9b"]
                                [:spells/by-id "93da1dbc-bc06-4895-a7e5-9e542a2c88e7"]]
                       :armor [[:armor/by-name "Leather"]]
                       :equipment [[:weapons/by-name "Katana"]
                                   [:weapons/by-name "Katana"]
                                   [:weapons/by-name "Longbow"]]}
                      {:id "8166a5df-613c-429f-bcd9-1c7d40e8db10"
                       :alignment :lawful-good
                       :abilities {:strength     14
                                   :dexterity    13
                                   :constitution 15
                                   :intelligence 11
                                   :wisdom       9
                                   :charisma     12}
                       :background :charlatan
                       :classes {:rogue {:experience 5375}}
                       :hit-points {:maximum 39 :current 39}
                       :money {:platinum 1130
                               :gold 3595
                               :electrum 0
                               :silver 120
                               :copper 0}
                       :name "Sir Elton John Hancock"
                       :race :human
                       :skill-proficiences #{:acrobatics :deception :persuasion :sleight-of-hand :stealth}
                       :spellcasting-ability :wisdom
                       :armor [[:armor/by-name "Leather"]]
                       :equipment [[:weapons/by-name "Crossbow, light"]
                                   [:weapons/by-name "Dagger"]
                                   [:weapons/by-name "Dagger"]]}]})
  #_ (local-storage :state))

#_ (def search-node
  (search/create-node
    {:spells {:ref :id
              :fields [:name :description]
              :documents (vals srd-spells/by-id)}
     :weapons {:ref :name
               :fields [:name]
               :documents (vals srd-weapons/by-name)}}))

#_ (doseq [r (search/search-index search-node [:spells :weapons] "Spear")]
  (cljs.pprint/pprint r)
  #_ (println (js/JSON.stringify r nil "  ")))

(defn keyword->name [k]
  (str/join
    " "
    (map str/capitalize
         (str/split (name k) #"-"))))

(defn n->currency
  ([n]
   (n->currency
     n
     [{:name :platinum :exchange-rate 1000}
      {:name :gold     :exchange-rate 100}
      {:name :electrum :exchange-rate 50}
      {:name :silver   :exchange-rate 10}
      {:name :copper   :exchange-rate 1}]))
  ([n denominations]
   (loop [m n
          col denominations
          results {}]
     (if-some [{:keys [exchange-rate name]} (first col)]
       (let [value (js/parseInt (/ m exchange-rate))]
         (recur (- m (* exchange-rate value))
                (drop 1 col)
                (assoc results name value)))
       results))))

(defn ability->modifier [n]
  (js/Math.floor
    (/ (- n 10)
       2)))

(defn experience->level [exp]
  (reduce
    (fn [level {:keys [experience] :as next-level}]
      (if (>= exp experience)
        next-level
        (reduced level)))
    (:levels @app-state)))

(defn n->level [n]
  (let [levels (get @app-state :levels)]
    (get levels (dec n))))

(defn n->next-level [n]
  (let [levels (get @app-state :levels)]
    (if-some [next-level (n->level (inc n))]
      next-level
      (n->level n))))

(defn character-experience [{:keys [classes]}]
  (reduce
    (fn [exp value]
      (let [[_ {:keys [experience]}] value]
        (+ exp experience)))
    0
    classes))

(defn character-level [character]
  (experience->level
    (character-experience character)))

(defn character-speed [{:keys [race]}]
  (get-in @app-state [:races race :base-speed]))

(defn character-abilities [{:keys [abilities race]}]
  (merge-with +
              abilities
              (get-in @app-state [:races race :ability-score-increase])))

(defn character-classes [{:keys [classes]}]
  (reduce
    (fn [s [class _]]
      (merge s (hash-map class (get-in @app-state [:classes class]))))
    {}
    classes))

(defn character-class [{:keys [classes] :as character}]
  (let [values (character-classes character)
        [class _] (first classes)]
    (get values (get character :active-class class))))

(defn class-attribute [{:keys [scale]} attr level]
  (get
    (reduce
      (fn [m conf]
        (merge-with
          (fn [l r]
            (if (set? l)
              (clojure.set/union l r)
              (+ l r)))
          m
          conf))
      {}
      (take level scale))
    attr))

(defn character-skills [{:keys [skill-proficiences] :as character} proficiency-bonus]
  (let [abilities (character-abilities character)]
    (reduce
      (fn [m [skill ability]]
        (assoc m skill (+ (ability->modifier (get abilities ability))
                          (cond
                            (contains? skill-proficiences skill) proficiency-bonus
                            :else 0))))
      {}
      (get @app-state :skills))))

(defn character-weapons [{:keys [proficiencies weapons] :as character} character-class proficiency-bonus]
  (let [abilities (character-abilities character)
        weapon-proficiencies (get-in character-class [:proficiencies :weapons])]
    (map (fn [{:keys [category class] :as weapon}]
           (let [ability (if (= class :melee) :strength :dexterity)
                 ability-modifier (ability->modifier (get abilities ability))]
             (merge-with
               +
               {:attack-bonus (+ ability-modifier
                               (if (contains? weapon-proficiencies category)
                                 proficiency-bonus
                                 0))
                :damage-bonus ability-modifier}
               weapon)))
         weapons)))

(defn character-armor [{:keys [armor] :as character}]
  (let [abilities (character-abilities character)]
    (map (fn [{:keys [armor-class armor-class-ability-modifiers] :as a}]
           (merge
             a
             {:armor-class-effective
              (+ armor-class
                 (min (reduce (fn [bonus ability]
                                (+ bonus (ability->modifier (get abilities ability))))
                              0
                              armor-class-ability-modifiers)
                      (get a :armor-class-ability-modifier-limit js/Number.MAX_VALUE)))}))
         armor)))

(defn character-armor-class [character]
  (let [abilities (character-abilities character)
        armor-class (reduce (fn  [ac {:keys [armor-class-effective]}]
                              (+ ac armor-class-effective))
                            0
                            (character-armor character))]
    (max armor-class
         (+ 10 (ability->modifier (get abilities :dexterity))))))

(defui ProgressBar
  Object
  (render [this]
    (let [{:keys [total current]} (om/props this)
          percent (str (js/Math.round
                         (* (/ current total) 100))
                       "%")]
      (dom/div #js {:className "progress-bar"}
        (dom/progress #js {:value current
                           :max total})
        (dom/div #js {:className "progress-bar-label"}
          (str current " / " total " (" percent ")"))))))

(def progress-bar (om/factory ProgressBar))

(defui Currency
  Object
  (render [this]
    (apply dom/ul #js {:className "currency inline pl0"}
      (reduce
        (fn [list-items denomination]
          (conj list-items
                (dom/li #js {:className (str "currency-" (name (first denomination)))}
                  (dom/svg #js {:height "12px"
                                :width "12px"}
                    (dom/rect #js {:height "100%"
                                   :width "100%"}))
                  (dom/span #js {:className "ml1"} (last denomination)))))
        []
        (get (om/props this) :money)))))

(def currency (om/factory Currency))

(defui Definition
  Object
  (render [this]
    (let [{:keys [formatter title value] :or {formatter identity}} (om/props this)]
      (dom/dl #js {:className "f6 lh-title mv2"}
        (dom/dt #js {:className "dib b"} (str title ":"))
        (dom/dd #js {:className "dib ml0 pl1 gray"} (formatter value))))))

(def definition (om/factory Definition))

(defui Definitions
  Object
  (render [this]
    (apply dom/div #js {:className "definitions"}
      (map definition (om/props this)))))

(def definitions (om/factory Definitions))

(defui Stat
  Object
  (render [this]
    (let [{:keys [formatter title value] :or {formatter identity}} (om/props this)]
       (dom/dl #js {:className "fl fn-l w-33 dib-l w-auto-l lh-title mr5-1 statistic"}
        (dom/dd #js {:className "f6 fw4 ml0"} title)
        (dom/dd #js {:className "f3 fw6 ml0"} (formatter value))))))

(def stat (om/factory Stat))

(defui Stats
  Object
  (render [this]
    (apply dom/div #js {:className "bb bt cf tc statistics"}
      (map stat (om/props this)))))

(def stats (om/factory Stats))

(defui Table
  Object
  (render [this]
    (let [{:keys [columns rows]} (om/props this)]
      (dom/table #js {:cellSpacing 0
                      :className "f6 w-100"}
        (dom/thead nil
          (apply dom/tr nil
            (map #(dom/th nil (:title %))
                 columns)))
        (apply dom/tbody #js {:className "lh-copy"}
          (map (fn [row]
                 (apply dom/tr nil
                   (map (fn [{:keys [formatter query] :or {formatter identity}}]
                          (dom/td nil (formatter (get-in row query) row)))
                        columns)))
               rows))))))

(def table (om/factory Table))

(defui Spell
  static om/Ident
  (ident [_ {:keys [id]}]
    [:spells/by-id id])
  static om/IQuery
  (query [_]
    '[:classes :description :level :material-components :name :range :school])
  Object
  (render [this]
    (let [{:keys [classes description level material-components range school] :as props} (om/props this)]
      (dom/div #js {:className "spell"}
         (dom/h3 #js {:className "spell-name mv2"} (get props :name))
         (dom/div #js {:className "i spell-meta-description"}
           (str/capitalize
             (str/join
               " "
               (remove nil? [(cond
                               (> level 0) (str (humanize/ordinal level) "-level"))
                             (name school)
                             (cond
                               (zero? level) "cantrip")]))))
         (dom/div #js {:className "spell-meta-data"}
           (definitions (map
                          (fn [{:keys [key] :as config}]
                            (merge
                              {:title (keyword->name key)
                               :value (get props key)}
                              (when-some [formatter (get config :formatter)]
                                {:formatter formatter})))
                          [{:key :casting-time}
                           {:key :range
                            :formatter (fn [v]
                                         (if (keyword? v)
                                           (keyword->name v)
                                           v))}
                           {:key :components
                            :formatter (fn [v]
                                         (str v (cond
                                                  (not (nil? material-components)) (str " (" material-components ")"))))}
                           {:key :duration}])))
        (dom/div #js {:className "lh-copy spell-description"
                      :dangerouslySetInnerHTML #js {:__html (md/md->html description)}})))))

(def spell (om/factory Spell {:keyfn :id}))

(defui Spells
  Object
  (render [this]
    (apply dom/div #js {:className "mt3 spells"}
      (map spell (om/props this)))))

(def spells-ui (om/factory Spells))

(defui AbsoluteValue
  Object
  (render [this]
    (let [value (om/props this)]
      (dom/span #js {:className (cond
                                  (< value 0) "red"
                                  (> value 0) "green")}
        (dom/span nil (str (when (> value 0) "+") value))))))

(def absolute-value (om/factory AbsoluteValue))

(defui Measurement
  Object
  (render [this]
    (let [{:keys [units value]} (om/props this)]
      (dom/span #js {:className "measurement"}
        (str value " " units)))))

(def measurement (om/factory Measurement))

(defui Character
  static om/Ident
  (ident [this {:keys [id]}]
    [:characters/by-id id])
  static om/IQuery
  (query [_]
    [:id
     :abilities
     :alignment
     :background
     :classes
     :hit-points
     :money
     :race
     :skill-proficiences
     :spellcasting-ability
     :spells
     :weapons])
  Object
  (render [this]
    (let [{:keys [alignment background classes hit-points race skill-proficiences] :as character} (om/props this)
          abilities (character-abilities character)
          experience (character-experience character)
          {:keys [level proficiency-bonus]} (experience->level experience)
          next-level (n->next-level level)
          current-class (character-class character)
          weapons (character-weapons character current-class proficiency-bonus)]
      (dom/div nil
        (dom/h1 #js {:className "f1 character-name mv1"}
          (:name character))
        (dom/ul #js {:className "character-description inline pl0"}
          (dom/li nil (str "Level " level))
          (when-not (nil? alignment)
            (dom/li nil
              (dom/a #js {:href "#"}
                (keyword->name alignment))))
          (dom/li nil
            (dom/a #js {:href "#"}
              (keyword->name race)))
          (dom/li nil
            (dom/a #js {:href "#"}
              (keyword->name background))))
        (currency character)
        (dom/div #js {:className "character-health character-progress-bar mt3 mb2"}
          (dom/div #js {:className "character-progress-bar-label"} "HP")
          (progress-bar
            {:current (:current hit-points)
             :total (:maximum hit-points)}))
        (dom/div #js {:className "character-experience character-progress-bar mt2 mb3"}
          (dom/div #js {:className "character-progress-bar-label"} "XP")
          (progress-bar
            {:current experience
             :total (get next-level :experience)}))
        (stats [{:title "Armor Class" :value (character-armor-class character)}
                {:title "Initiative"  :value (ability->modifier (get abilities :dexterity))}
                {:title "Speed"       :value (measurement
                                               {:units "ft"
                                                :value (character-speed character)})}])
        (stats
          (map
             (fn [k]
               {:formatter
                (fn [v]
                  (let [m (ability->modifier v)]
                    (dom/span #js {:className "ability"}
                      (absolute-value m)
                      (dom/span #js {:className "f5 ml1 gray"} v))))
                :title (keyword->name k)
                :value (get abilities k)})
             (keys abilities)))
        (stats (reduce
                 conj
                 [{:title "Proficiency Bonus" :value (absolute-value proficiency-bonus)}]
                 (map (fn [ability]
                        {:title (keyword->name ability)
                         :value (+ (ability->modifier (get abilities ability))
                                   proficiency-bonus)})
                      (get current-class :saving-throws))))
        (table {:columns [{:title "Name"   :query [:name]}
                          {:title "Attack" :query [:attack-bonus] :formatter absolute-value}
                          {:title "Damage"
                           :query [:damage :base]
                           :formatter (fn  [damage weapon]
                                        (str (name damage)
                                             " "
                                             (name (get-in weapon [:damage :type]))))}
                          {:title "Bonus"  :query [:damage-bonus] :formatter absolute-value}]
                :rows weapons})
        (stats
          (sort-by :sort-key (map (fn [[skill value]]
                                    (let [contained? (contains? skill-proficiences skill)]
                                      {:sort-key skill
                                       :title (dom/span
                                                #js {:className (if contained? "b" "gray")}
                                                (keyword->name skill)) 
                                  :value (dom/span
                                           (when-not contained?
                                             #js {:className "gray"})
                                           value)}))
                               (character-skills character proficiency-bonus))))
        (when-some [spellcasting-ability (get character :spellcasting-ability)]
          (stats
            [{:title "Ability"
              :value (keyword->name spellcasting-ability)}
             {:title "Difficulty Class"
              :value (+ 8
                        proficiency-bonus
                        (ability->modifier (get abilities spellcasting-ability)))}
             {:title "Attack Bonus"
              :value (absolute-value
                       (+ (ability->modifier (get abilities spellcasting-ability))
                          proficiency-bonus))}]))
        (when-some [character-spells (get character :spells)]
          (dom/section #js {:className "character-spells"}
            #_ (dom/h2 #js {:className "f2 bb"} "Spells")
            (spells-ui character-spells)))))))

(def character (om/factory Character {:keyfn :id}))

(defui Characters
  static om/IQuery
  (query [_]
    `[{:characters ~(om/get-query Character)}])
  Object
  (render [this]
    (apply dom/section nil
      (map #(dom/div nil
              (dom/a #js {:href (str "/characters/" (:id %))} (:name %)))
           (om/props this)))))

(def characters (om/factory Characters))

(defui Profile
  static om/IQueryParams
  (params [_]
    {:id ""})
  static om/IQuery
  (query [_]
    '[(:character {:id ?id}) (:spells {:character-id ?id}) (:weapons {:character-id ?id}) (:armor {:character-id ?id})])
  Object
  (render [this]
    (let [{:keys [armor spells weapons] :as props} (om/props this)]
      (dom/div nil
        (character (merge (get props :character)
                          {:armor armor
                           :spells spells
                           :weapons weapons}))))))

(defui Search
  static om/IQueryParams
  (params [_]
    {:query ""})
  static om/IQuery
  (query [_]
    '[(:search/results {:query ?query})])
  Object
  (render [this]
    (let [{:keys [results]} (om/props this)]
      (dom/div nil
         (dom/input #js {:type "text"
                         :value (:query (om/get-params this))
                         :onInput (fn [e]
                                    (om/set-query! this
                                      {:params {:query (.. e -target -value)}}))})))))

(def search-ui (om/factory Search))

(defui Index
  static om/IQuery
  (query [_]
    '[:characters])
  Object
  (render [this]
    (let [{:keys [characters] :as props} (om/props this)]
      (dom/div #js {:className "screen-index mt5"}
        (dom/object (clj->js {:data "/images/maze.svg"
                              :style {:alignSelf "center"}
                              :type "image/svg+xml"}))
        (dom/p (clj->js {:className "f6 i system-serif"
                         :style {:alignSelf "center"}})
               "\"It is best for you to follow me...\"")
        (dom/a #js {:className "f6 link dim ba ph3 pv2 mb2 dib black tc"
                    :href "/characters"} "Characters")
        (dom/a #js {:className "f6 link dim ba ph3 pv2 mb2 dib black tc"
                    :href "/monsters"} "Monsters")
        (dom/a #js {:className "f6 link dim ba ph3 pv2 mb2 dib black tc"
                    :href "/spells"} "Spells")
        (dom/a #js {:className "f6 link dim ba ph3 pv2 mb2 dib black tc"
                    :href "/items"} "Items")))))

(defmulti read om/dispatch)

(defmethod read :default
  [{:keys [state] :as env} key params]
  (let [st @state]
    (if-let [[_ value] (find st key)]
      {:value value}
      {:value :not-found})))

(defmethod read :index
  [{:keys [state] :as env} _ params]
  (let [st @state]
    (if-let [characters (get st :characters)]
      {:value {:characters characters}}
      {:value {:characters :not-found}})))

(defmethod read :character-profile
  [{:keys [state] :as env} _ _]
  (let [st @state
        id (get-in st [:compassus.core/route-params :id])]
    (if-let [character (first (filter #(= (:id %) id)
                                      (:characters st)))]
      {:value {:character character
               :armor (map (partial get-in st) (:armor character))
               :spells (map (partial get-in st) (:spells character))
               :weapons (map (partial get-in st)
                             (filter
                               (fn [[k v]]
                                 (= (namespace k) "weapons"))
                               (:equipment character)))}}
      {:value nil})))

(defmethod read :character
  [{:keys [state] :as env} key {:keys [id]}]
  (let [st @state]
    (if-let [character (first (filter #(= (:id %) id)
                                      (:characters st)))]
      {:value character}
      {:value :not-found})))

(defmethod read :spells
  [{:keys [state] :as env} key {:keys [character-id] :as params}]
  (let [st @state]
    (if-let [character (first (filter #(= (:id %) character-id)
                                      (:characters st)))]
      {:value (map (partial get-in st) (:spells character))}
      {:value nil})))

(defmethod read :weapons
  [{:keys [state] :as env} key {:keys [character-id] :as params}]
  (let [st @state]
    (if-let [character (first (filter #(= (:id %) character-id)
                                      (:characters st)))]
      {:value (map (partial get-in st)
                   (filter
                     (fn [[k v]]
                       (= (namespace k) "weapons"))
                     (:equipment character)))}
      {:value :not-found})))

(defmethod read :armor
  [{:keys [state] :as env} key {:keys [character-id] :as params}]
  (let [st @state]
    (if-let [character (first (filter #(= (:id %) character-id)
                                      (:characters st)))]
      {:value (map (partial get-in st)
                   (:armor character))}
      {:value :not-found})))

(def reconciler
  (om/reconciler
    {:state app-state
     :parser (compassus/parser {:read read})}))

(defroute index-route "/" []
  (compassus/set-route! reconciler :index))

(defroute characters-route "/characters" []
  (compassus/set-route! reconciler :characters))

(defroute character-route "/characters/:id" [id]
  (compassus/set-route! reconciler :character-profile {:params {:compassus.core/route-params {:id id}}}))

(def app
  (compassus/application
    {:routes {:index Index
              :characters Characters
              :character-profile Profile}
     :index-route :index
     :reconciler reconciler
     :mixins [(compassus/did-mount (fn [_]
                                     (accountant/configure-navigation!
                                       {:nav-handler #(secretary/dispatch! %)
                                        :path-exists? #(secretary/locate-route %)})))]}))

(compassus/mount! app (gdom/getElement "app"))

(when-some [service-worker (.. js/navigator -serviceWorker)]
  (.register service-worker "/sw.js"))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
