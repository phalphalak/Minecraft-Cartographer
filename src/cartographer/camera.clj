(ns jogl.camera)

(set! *warn-on-reflection* true)

(defprotocol PCamera
  (forward [this distance])
  (backward [this distance])
  (up [this distance])
  (down [this distance])
  (strafe-left [this distance])
  (strafe-right [this distance])
  (pitch-up [this amount])
  (pitch-down [this amount])
  (yaw-left [this amount])
  (yaw-right [this amount])
  (get-look-at [this]))

(defrecord Camera [x y z pitch yaw]
  PCamera
  (forward [this distance]
    (assoc
        (assoc this :x (+ x (* distance (Math/cos yaw))))
      :z (+ z (* distance (Math/sin yaw)))
      ))
  (backward [this distance] (forward this (- distance)))
  (up [this distance] (assoc this :y (+ y distance)))
  (down [this distance] (up this (- distance)))
  (strafe-right [this distance]
    (assoc
        (assoc this :x (+ x (* distance (Math/cos (+ (/ Math/PI 2) yaw)))))
      :z (+ z (* distance (Math/sin (+ (/ Math/PI 2) yaw))))))
  (strafe-left [this distance] (strafe-right this (- distance)))
  (pitch-up [this amount] (assoc this :pitch (max (- (/ Math/PI 2)) (min (/ Math/PI 2) (+ pitch amount)))))
  (pitch-down [this amount] (pitch-up this (- amount)))
  (yaw-right [this amount] (assoc this :yaw (+ yaw amount)))
  (yaw-left [this amount] (yaw-right this (- amount)))
  (get-look-at [this]
    (let [cos-pitch (Math/abs (Math/cos pitch))]
      (vec (map +
                [x y z]
                [(* cos-pitch (Math/cos yaw)) (Math/sin pitch)  (* cos-pitch (Math/sin yaw))]))))
  )
