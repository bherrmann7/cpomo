(ns cpomo.screen
  (:import (java.awt Robot Rectangle Toolkit GraphicsEnvironment Color AlphaComposite))
  (:import (java.awt.image BufferedImage))
  (:import (java.awt.event ActionListener))
  (:import (javax.swing JWindow Timer)))

(defn take-screenshot [] 
  (let [robot (Robot.)]
    (.createScreenCapture robot 
      (Rectangle. (.. Toolkit getDefaultToolkit getScreenSize)))))

(defn image-panel [image x y]
  (proxy [JWindow] []
    (paint [g]
      (.drawImage g image x y this))))

(defn print-is-fullscreen-supported [g]
  (println (.isFullScreenSupported g)))

(defn show-image [image]
  (let [image-panel (image-panel image 0 0) 
        graphics (GraphicsEnvironment/getLocalGraphicsEnvironment)]
    (doto image-panel
      (.setAlwaysOnTop true)
      (.setVisible true))
    (doto (.getDefaultScreenDevice graphics)
      (.setFullScreenWindow image-panel)) image-panel))

(defn translucentify-image [img transparency]
  (let [image (BufferedImage. (.getWidth img) (.getHeight img) BufferedImage/TRANSLUCENT)
        g (.createGraphics image)]
    (.setComposite g (AlphaComposite/getInstance AlphaComposite/SRC_OVER transparency))
    (.drawImage g img nil 0 0)
    (.dispose g) image))

(defn release-screen [screen]
  (proxy [ActionListener] []
    (actionPerformed [event]
      (.dispose screen))))

(defn lock-screen []
  (let [screenshot (take-screenshot) 
        screen (show-image (translucentify-image screenshot 0.8))
        timer (Timer. 5000 (release-screen screen))]
    (.setRepeats timer false)
    (.start timer)))
