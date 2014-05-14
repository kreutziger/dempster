(ns dempster.core
  (:gen-class))

(defn createBasicMeasure
  "creates an empth basic measure
  input - size - lenght of the basic measure
  input - size - length of the basic measure
  input - evidence - evidence between 0 and 1"
  ([size]
    (loop [iteration 1 bm []]
      (if (> iteration size)
        (vector (conj bm 1.0))
        (recur (inc iteration) (conj bm 1))
      )
    )
  )
  ([size evidence]
    (loop [iteration 1 bm []]
      (if (> iteration size)
        (vector (conj bm evidence))
        (recur (inc iteration) (conj bm 1))
      )
    )
  )
)

(defn printSingleBasicMeasure 
  "prints a single basic measure out
  input - basic measure - one bm to print out"
  [bm]
  (loop [[head & tail] bm output []]
    (if (integer? head)
      (recur tail (conj output (str head)))
      (println 
        (clojure.string/join "" 
          (list "m([" (clojure.string/join "" output) "]) =\t" (format "%.3f" head))
        )
      )
    )
  )
)

(defn printBasicMeasure
  "prints multiple basic measures out
  input - basic measures - vector of bm to print out"
  [bm]
  (println "Basic Measure:")
  (loop [bm_remaining bm]
    (let [[head & tail] bm_remaining]
      (if (vector? head)
        (do 
          (printSingleBasicMeasure head) 
          (when 
            (not (nil? tail)) 
            (recur tail)
          )
        )
        (printSingleBasicMeasure bm_remaining)
      )
    )
  )
)

(defn plausibility 
  "processes the plausbility of a certain alternative
  input - basic measures - vector of bm to process the plausibility from
  input - index - index of the alternative to process the plausibility"
  [bm i]
  (loop [bm_remaining bm p 0.0]
    (let [[head & tail] bm_remaining]
      ; take i-th value of head, if i is out of bound return 0
      (if (vector? head)
        (if (= (get head (- i 1) 0) 1)
          (recur tail (+ p (peek head)))
          (recur tail p)
        )
        ; return p if head is no vector but the end
        p
      )
    )
  )
)

(defn doubt
  "processes the doubt via the formular 1 - plausibility(x)
  input - basic measures - vector of bm to process the doubt from
  input - index - index of the alternative to process the doubt"
  [bm i]
  (- 1 (plausibility bm i))
)

(defn belief 
  "processes the belief out of a vector of basic measures
  input - basic measures - vector of bm to process the belief from
  input - index - index of the alternative to process the belief"
  [bm i]
  (loop [bm_remaining bm b 0.0]
    (let [[head & tail] bm_remaining]
      (if (vector? head)
        (if (and 
              ; count has to be 2 because of the positive float
              (= 2 (count (filterv pos? head)))
              (= (get head (- i 1) 0) 1)
            )
          (recur tail (+ b (peek head)))
          (recur tail b)
        )
        b
      )
    )
  )
)

(defn addBasicMeasure
  "adds a new measure to an existing measure
  input - existingMeasure - bm which should be added to other measures
  input - toAdd - vector of bm which receive a new bm"
  ([existingMeasure toAdd]
    (let [omega (peek existingMeasure)]
      (if (= (count omega) (count toAdd))
        (if (every? #(or (= 0 %) (= 1 %)) (pop toAdd))
          (if (and (pos? (peek toAdd)) (<= (peek toAdd) 1))
            (conj
              (conj (pop existingMeasure) toAdd)
              (conj (pop omega) (- (peek omega) (peek toAdd)))
            )
            (println "The evidence value of a measure should be between 0 and 1")
          )
          (println "A measure should not contain other values than 0 or 1 (except last entry)")
        )
        (println "The size of the entries must be equal")
      )
    )
  )
)

(defn listwiseAnd
  "connects to lists with an logical and
  input - firstmeasure - first bm which gets anded
  input - secondmeasure - second bm which gets anded"
	([firstmeasure secondmeasure]
		(loop [fm firstmeasure sm secondmeasure new []]
			(let [[fm_head & fm_tail] fm [sm_head & sm_tail] sm]
				(if (empty? fm)
					new
					(recur
						fm_tail
						sm_tail
						;create a new list which is an logical list and representation of the source lists
						(conj new (bit-and (int fm_head) (int sm_head)))
					)
				)
			)
		)
	)
)

(defn accumulate
  "accumulates one measure with a list of measures
  input measure - the bm to accumulate to other measures
  input measurelist - list of measures"
	([measure measurelist]
		(loop [m measure ml measurelist new []]
			(let [[ml_head & ml_tail] ml]
				(if (empty? ml)
					new
					(recur
						m
						ml_tail
						;accumulate measures and add to result list
						(conj 
                          new 
                         (conj (listwiseAnd (pop m) (pop ml_head)) (* (peek m) (peek ml_head)))
                        )
					)
				)
			)
		)
	)
)

(defn getRawMeasures
  "get the unclean measures
  this means the resulting list contains duplicates and empty lists
  input - firstmeasure - first measure
  input - secondmeasure - second measure"
	([firstmeasure secondmeasure]
		(loop [fm firstmeasure sm secondmeasure new []]
			(let [[fm_head & fm_tail] fm]
				(if (empty? fm)
					new
					(recur
						fm_tail
						sm
						;concatenate and cast to vector
						(into (vector) (concat new (accumulate fm_head sm)))
					)
				)
			)
		)
	)
)

(defn mergeEntries
  "merge all entries, that are equal to the given one
  input - measure - measure to be added to the measurelist
  input - measurerList - vector of measures"
	([measure measureList]
		(loop [m measure ml measureList newMeasure m]
			(let [[ml_head & ml_tail] ml]
				(if (empty? ml)
					newMeasure
					(if (= (pop ml_head) (pop m))
						;sums up the evidence values of identical measures
						(recur m ml_tail (conj (pop newMeasure) (+ (peek newMeasure) (peek ml_head))))
						(recur m ml_tail newMeasure)
					)
				)
			)	
		)
	)
)

(defn removeDuplicates
  "removes all duplicates and sums up the evidences
  input - measurelist - vector of measures"
	([measurelist]
		(loop [ml measurelist  cleanedList[]]
			(let [[ml_head & ml_tail] ml]
				(if (empty? ml)
					cleanedList
					(recur
						;deletes all entries which are already evaluated
						(filter #(not= (pop ml_head) (pop %)) ml_tail)
						(conj cleanedList (mergeEntries ml_head ml_tail))
					)
				)
			)
		)
	)
)

(defn getConflict
  "identify an empty measure in the measure list
  returns the evidence of the empty measure or 0 if the was none
  input - measureList - vector of measures"
	([measureList]
		(loop [ml measureList]
			(let [[ml_head & ml_tail] ml]
				(if (empty? ml)
					0
					(if (= (.indexOf ml_head 1) -1)
						(peek ml_head)
						(recur ml_tail)
					)
				)
			)
		)
	)
)

(defn resolveConflict
  "resolves the conflict for a set of measures
  input - measureList - vector of measures
  input - conflict - float of conflict between 0.0 and 1.0"
	([measureList conflict]
		(loop [ml measureList resolvedList []]
			(let [[ml_head & ml_tail] ml correction (/ 1.0 (- 1.0 conflict))]
				(if (empty? ml)
					resolvedList
					(if (= (.indexOf ml_head 1) -1)
						(recur ml_tail resolvedList)
						(recur ml_tail 
                          (conj resolvedList (conj (pop ml_head) (* (peek ml_head) correction)))
                        )
					)
				)
			)
		)
	)
)

(defn getAccumulatedMeasures
  "get a list of all accumulated measures
  without duplicates and recognition of conflicts
  input - firstmeasure - measure to be accumulated
  input - secondmeasure - measure to be accumulated"
	([firstmeasure secondmeasure]
		(let 
          [measureList (removeDuplicates (getRawMeasures firstmeasure secondmeasure)) 
           conflict (getConflict measureList)
          ]
			(if (= 0 conflict)
				measureList
				(resolveConflict measureList conflict)
			)
		)
	)
)

(defn overallValues
  "printout plausibility, belief and doubt for all values
  input - measures - vector of measures of which will be processed for
  plausibility, belief and doubt"
  [measures]
  (when (vector? measures)
    (println " # : Pl(x)  |  B(x)   | Z(x)")
    (loop [bm measures iteration 0]
      (let [[head & tail] bm]
        (when (not (nil? tail))
          (print (format " %d : %5.3f  |  %5.3f  | %5.3f \n" iteration (plausibility 
                measures iteration) (belief measures iteration) (doubt measures iteration)))
          (recur tail (inc iteration))
        )
      )
    )
  )
)

(defn createMeasures
  "convenient function to create a measure out of a multiple measures
  input - length - length of the measures
  input - measures - vector of measures, which are one measure"
  [length measures]
  (let [[head & tail] measures]
	(if (nil? head)
      (createBasicMeasure length)
      (addBasicMeasure (createMeasures length tail) head)
    )
  )
)

(defn -main
  "Test implementation of dempster shafer rule"
  [& args]
  (let 
    [entry1 [1 0 1 1 0 0 0 0.7] entry3 [1 0 0 1 1 1 1 0.85]
     entry4 [1 0 1 0 0 0 0 0.4] entry5 [0 1 0 0 1 0 0 0.23] 
     entry6 [0 0 0 0 0 0 1 0.12]
    ]
    (def m (createMeasures (- (count entry1) 1) [entry1]))
    ;comments are still there, so you better understand createMeasures
    ;(def m (addBasicMeasure (createBasicMeasure 7) entry1))
    ;(def m2 (addBasicMeasure (createBasicMeasure 7) entry3))
    (def m2 (createMeasures (- (count entry3) 1) [entry3]))
    (def m3 (createMeasures (- (count entry4) 1) [entry4 entry5 entry6]))
    ;(def m3 
    ;  (addBasicMeasure 
    ;    (addBasicMeasure 
    ;      (addBasicMeasure (createBasicMeasure 7) entry4)
    ;    entry5)
    ;  entry6)
    ;)
    (def res (getAccumulatedMeasures m m2))
    (def res2 (getAccumulatedMeasures res m3))
    
    (printBasicMeasure m)
    (printBasicMeasure m2)
    (println "accumulate...")
    (printBasicMeasure res)
    (printBasicMeasure m3)
    (println "accumulate...")
    (printBasicMeasure res2)
    (overallValues res2)
  )
)
