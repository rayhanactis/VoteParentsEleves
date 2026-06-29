package com.rayhanactis.voteparentseleves.scan

import com.journeyapps.barcodescanner.CaptureActivity

// Sous-classe vide de l'activité de scan zxing. Son seul rôle est de porter
// android:screenOrientation="sensorPortrait" dans le manifeste androidMain :
// la CaptureActivity par défaut de zxing s'ouvre en paysage, ce qui n'est pas
// adapté au scan du QR projeté par l'école depuis un téléphone tenu en portrait.
class PortraitCaptureActivity : CaptureActivity()
