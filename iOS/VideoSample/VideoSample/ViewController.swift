//
//  ViewController.swift
//  VideoSample
//

import Foundation
import UIKit
import TXLiteAVSDK_TRTC

class ViewController: UIViewController {
    @IBOutlet weak var remoteVideoView: UIView!
    @IBOutlet weak var remoteUserLabel: UILabel!
    @IBOutlet weak var joinButton: UIButton!
    private var trtcCloud: TRTCCloud = TRTCCloud.sharedInstance()
    private var joined = false

    override func viewDidLoad() {
        super.viewDidLoad()

        remoteUserLabel.text = ""
        remoteVideoView.layer.opacity = 0.3
        joinButton.tintColor = UIColor.systemGreen
    }

    @IBAction func joinButtonTouched(_ sender: Any) {
        if joined {
            exitRoom()
        } else {
            enterRoom()
        }
    }

    private func enterRoom() {
        let roomId: Int = 1
        let userId: String = "iOS demo1"
        let isFrontCamera = true
        
        trtcCloud.delegate = self
        trtcCloud.startLocalPreview(isFrontCamera, view: view)
        let params = TRTCParams()
        params.sdkAppId = UInt32(SDKAPPID)
        params.roomId = UInt32(roomId)
        params.userId = userId
        params.role = .anchor
        params.userSig = TrtcUserSig.genTestUserSig(identifier: userId) as String
        trtcCloud.enterRoom(params, appScene: .videoCall)
        
        let encParams = TRTCVideoEncParam()
        encParams.videoResolution = ._640_360
        encParams.videoBitrate = 550
        encParams.videoFps = 15
        trtcCloud.setVideoEncoderParam(encParams)

        trtcCloud.startLocalPreview(isFrontCamera, view: view)
        trtcCloud.startLocalAudio(.music)
    }

    private func exitRoom() {
        trtcCloud.exitRoom()
        trtcCloud.stopLocalPreview()
        trtcCloud.stopLocalAudio()
        trtcCloud.delegate = self
    }
}

extension ViewController: TRTCCloudDelegate {
    func onEnterRoom(_ result: Int) {
        print("*** onEnterRoom: result: \(result)")
        joined = true
        joinButton.setTitle("Leave", for: .normal)
        joinButton.tintColor = UIColor.red
    }
    
    func onExitRoom(_ reason: Int) {
        print("*** onExitRoom: reason: \(reason)")
        joined = false
        joinButton.setTitle("Join", for: .normal)
        joinButton.tintColor = UIColor.systemGreen
    }
    
    func onUserVideoAvailable(_ userId: String, available: Bool) {
        print("*** onUserAudioAvailable: userId: \(userId), available: \(available)")
        if available {
            trtcCloud.startRemoteView(userId, streamType:.small, view: remoteVideoView)
            remoteVideoView.layer.opacity = 1
            remoteUserLabel.text = userId
        } else {
            trtcCloud.stopRemoteView(userId, streamType: .small)
            remoteVideoView.layer.opacity = 0.3
            remoteUserLabel.text = ""
        }
    }
    
    func onError(_ errCode: TXLiteAVError, errMsg: String?, extInfo: [AnyHashable : Any]?) {
        if let errMsg = errMsg {
            print("*** onError: \(errCode): \(errMsg)")
        } else {
            print("*** onError: \(errCode): ?")
        }
    }
}
