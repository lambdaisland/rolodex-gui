(ns rolodex.styles
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.selectors :refer [before]]
            [garden.units :refer [px]]))

(defn grey
  ([n]
   (grey n 1.0))
  ([n a]
   (apply format "rgba(%d,%d,%d,%.2f)" (concat (map #(int (- 255 (* 255/100 %))) [(- n 1) n (- n 3)]) [a]))))

(defn blue
  ([n]
   (blue n 1.0))
  ([n a]
   (apply format "rgba(%d,%d,%d,%.2f)" (concat (map #(int (- 255 (* 255/150 %))) [(- n 0) (- n 10) (- n 50)]) [a]))))

(defstylesheet screen
  {:output-to "resources/public/css/screen.css"}
  [:body
   {:font-family "sans-serif"
    :font-size (px 20)
    :line-height 1.5
    :color (grey 88)
    :background-color (grey 0)}]

  [:pre {:white-space "pre-wrap"}]
  [:button {:background-color (blue 90)
            :border "0"
            :padding "0.5rem"
            :font-size "1.3rem"
            :box-sizing "border-box"
            :color (grey 0)}]
  [:button:active {:background-color (blue 40)}]
  [:button:disabled {:background-color (grey 20)
                     :filter "grayscale(100%)"}]
  [:.app {:margin "2rem"}]
  [:.path {:font-size "1.3rem"
           :background-color (grey 7)
           :padding "0.5rem"
           :box-sizing "border-box"
           }]
  [:.rolodex {:display "flex"
              :flex-direction "row"}]
  [:.header {:display "flex"
             :margin "0.5rem 0 1rem 0"
             :font-size "2.5rem"}
   [:.brand {:flex "0 0 350px"
             :background-color (blue 90)
             :color (grey 2)
             :font-size "2.5rem"
             :padding "0.5rem"
             :box-sizing "border-box"}]
   [:.search {:flex "1 1 100px"
              :border (str "1px solid " (blue 90))
              :margin-left "1rem"
              :padding "0 1rem"
              :font-size "2.5rem"}]]
  [:.name-list-wrapper {:flex "0 1 350px"}
   [:.buttons {:display "flex"
               :justify-content "space-between"
               :margin-bottom "0.5rem"}
    [:* {:flex "0 0 49%"}]]]
  [:.name-list {:display "flex"
                :flex-direction "column"
                :margin-bottom "0.5rem"}
   [:button {:margin-bottom "0.5rem"}]

   [:.initial:first-child {:border "0"}]
   [:.initial {:display "flex"
               :flex "0 0 "
               :flex-direction "row"
               :background-color (grey 10)
               :border-top (str "1px solid " (grey 80 0.08))}
    [:.letter {:flex "0 0 2em"
               :padding-left "1em"
               :padding-top "0.2em"}]
    [:.contacts {:flex "1 1 300px"
                 :background-color (grey 5)
                 :padding "0.1em 0"}
     [:.contact {:padding "0.1em 1em"
                 :cursor "pointer"}]]]]
  [:.card {:flex "2 2 500px"
           :background-color (grey 0)
           :padding-left "1rem"
           }
   [:.card-entry {:display "flex"
                  :justify-content "flex-start"
                  :align-items "stretch"
                  :border-bottom (str "1px solid " (grey 10))
                  :line-height "2"
                  }
    [:.label {:flex "0 0 120px"
              :padding-right "1em"
              :justify-self "center"}]
    [:.value {:flex "5 5 500px"
              :padding-left "1em"}]]
   [:button.clear {:margin-top "0.5rem"
                   :margin-right "0"
                   :float "right"}]
   [:button {:margin-right "0.5rem"}]
   [:.rud-butts {:display "flex"
                 :margin-bottom "0.5rem" }]
   [:.uuid {:color (grey 20)
            :font-size "1rem"
            :line-height "4"}]
   [:pre {:color (grey 20)
          :font-size "1.5rem"}]]
  [:.selected {:background-color (grey 64)
               :color (grey 4)}]
  )
