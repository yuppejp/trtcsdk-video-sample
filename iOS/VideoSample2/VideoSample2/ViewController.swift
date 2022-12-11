//
//  ViewController.swift
//  VideoSample2
//

import Foundation
import UIKit
import TXLiteAVSDK_TRTC

class ViewController: UIViewController {
    @IBOutlet weak var remoteView1: UIView!
    @IBOutlet weak var remoteView2: UIView!
    @IBOutlet weak var remoteView3: UIView!
    @IBOutlet weak var remoteMic1: UIImageView!
    @IBOutlet weak var remoteMic2: UIImageView!
    @IBOutlet weak var remoteMic3: UIImageView!
    @IBOutlet weak var remoteUserId1: UILabel!
    @IBOutlet weak var remoteUserId2: UILabel!
    @IBOutlet weak var remoteUserId3: UILabel!
    
    @IBOutlet weak var ControlPanelView: UIView!
    @IBOutlet weak var userNameText: UILabel!
    @IBOutlet weak var roomNameText: UILabel!
    @IBOutlet weak var videoImage: UIImageView!
    @IBOutlet weak var videoSwitchImage: UIImageView!
    @IBOutlet weak var audioImage: UIImageView!
    @IBOutlet weak var joinButton: UIButton!
    
    private var remoteViews: [UIView] = []
    private var remoteMicImages: [UIImageView] = []
    private var remoteUserLabels: [UILabel] = []
    private var trtcCloud: TRTCCloud = TRTCCloud.sharedInstance()
    private var roomId : Int = 1
    private var userId : String = ""
    private var joined = false
    private var audioAvailable = true
    private var videoAvailable = true
    private var isFrontCamera = true
    private var remoteUsers: [RemoteUser] = []
    private var maxRemoteUserCount = 3

    override func viewDidLoad() {
        super.viewDidLoad()
        
        remoteViews = [remoteView1, remoteView2, remoteView3]
        remoteMicImages = [remoteMic1, remoteMic2, remoteMic3]
        remoteUserLabels = [remoteUserId1, remoteUserId2, remoteUserId3]
        
        videoImage.isUserInteractionEnabled = true
        videoImage.addGestureRecognizer(
            UITapGestureRecognizer(target: self, action: #selector(ViewController.videoTapped(_:))))
        
        videoSwitchImage.isUserInteractionEnabled = true
        videoSwitchImage.addGestureRecognizer(
            UITapGestureRecognizer(target: self, action: #selector(ViewController.videoSwitchTapped(_:))))
        
        audioImage.isUserInteractionEnabled = true
        audioImage.addGestureRecognizer(
            UITapGestureRecognizer(target: self, action: #selector(ViewController.audioTapped(_:))))
        
        ControlPanelView.layer.cornerRadius = 8
        ControlPanelView.backgroundColor = UIColor.white.withAlphaComponent(0.3)
        
        // debug
//        for i in 0..<3 {
//            let userId = "user\(i + 1)"
//            let user = RemoteUser(userId: userId)
//            remoteUsers.append(user)
//        }
        setupTRTCCloud()
        updateControlPanelView()
        updateRemoteView()

        if videoAvailable {
            trtcCloud.startLocalPreview(isFrontCamera, view: view)
        }
    }

    @IBAction func joinButtonTouched(_ sender: Any) {
        if joined {
            exitRoom()
        } else {
            enterRoom()
        }
    }

    @objc func videoTapped(_ sender: UITapGestureRecognizer) {
        videoAvailable.toggle()
        if videoAvailable {
            videoImage.image = UIImage(systemName: "video.fill")
            trtcCloud.muteLocalVideo(.big, mute: false)
            //trtcCloud.startLocalPreview(isFrontCamera, view: view)
            view.layer.opacity = 1
        } else {
            videoImage.image = UIImage(systemName: "video.slash.fill")
            trtcCloud.muteLocalVideo(.big, mute: true)
            //trtcCloud.stopLocalPreview()
            view.layer.opacity = 0.3
        }
    }

    @objc func videoSwitchTapped(_ sender: UITapGestureRecognizer) {
        isFrontCamera.toggle()
        trtcCloud.getDeviceManager().switchCamera(isFrontCamera)
    }

    @objc func audioTapped(_ sender: UITapGestureRecognizer) {
        audioAvailable.toggle()
        if audioAvailable {
            audioImage.image = UIImage(systemName: "speaker.fill")
            trtcCloud.muteLocalAudio(false)
        } else {
            audioImage.image = UIImage(systemName: "speaker.slash.fill")
            trtcCloud.muteLocalAudio(true)
        }
    }

    private func updateRemoteView() {
        // hide once
        for remoteView in remoteViews {
            remoteView.layer.opacity = 0
        }

        for (i, user) in remoteUsers.enumerated() {
            if i >= maxRemoteUserCount {
                return // TODO: Allow flexibility in number of participants
            }
            user.videoView = remoteViews[i]
            user.micImage = remoteMicImages[i]
            user.userIdLabel = remoteUserLabels[i]
            user.userIdLabel.text = user.userId
            user.updateRemoteVideo(available: user.videoAvailable)
        }
    }

    private func updateControlPanelView() {
        userNameText.text = userId
        roomNameText.text = "Room: #\(String(roomId)) (+\(remoteUsers.count) Joined)"
        
        if joined {
            joinButton.setTitle("Leave", for: .normal)
            joinButton.tintColor = UIColor.red
        } else {
            joinButton.setTitle("Join", for: .normal)
            joinButton.tintColor = UIColor.systemGreen
        }
    }

    private func setupTRTCCloud() {
        trtcCloud.delegate = self
        userId = generator(6)
    }
    
    private func enterRoom() {
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
        trtcCloud.startLocalAudio(.music)
    }

    private func exitRoom() {
        // clenup the remote view
        for remoteUser in remoteUsers {
            remoteUser.updateRemoteVideo(available: false)
        }
        remoteUsers.removeAll()
        updateRemoteView()

        trtcCloud.exitRoom()
        trtcCloud.stopLocalPreview()
    }

    private func generator(_ length: Int) -> String {
        let letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var randomString = ""
        for _ in 0 ..< length {
            randomString += String(letters.randomElement()!)
        }
        return randomString
    }
}

extension ViewController: TRTCCloudDelegate {

    func onEnterRoom(_ result: Int) {
        print("*** onEnterRoom: result: \(result)")
        joined = true
        updateControlPanelView()
    }
    
    func onExitRoom(_ reason: Int) {
        print("*** onExitRoom: reason: \(reason)")
        joined = false
        updateControlPanelView()
    }
    
    func onRemoteUserEnterRoom(_ userId: String) {
        print("*** onRemoteUserEnterRoom: userId: \(userId)")
        let user = RemoteUser(userId: userId)
        remoteUsers.append(user)
        updateRemoteView()
        updateControlPanelView()
    }

    func onRemoteUserLeaveRoom(_ userId: String, reason: Int) {
        print("*** onRemoteUserLeaveRoom: userId: \(userId), reason: \(reason)")
        if let user = remoteUsers.first(where: { $0.userId == userId }) {
            user.updateRemoteVideo(available: false)
            remoteUsers.removeAll(where: { $0.userId == userId })
        }
        updateRemoteView()
        updateControlPanelView()
    }
    
    func onUserAudioAvailable(_ userId: String, available: Bool) {
        print("*** onUserAudioAvailable: userId: \(userId), available: \(available)")
        if let user = remoteUsers.first(where: { $0.userId == userId }) {
            user.updateRemoteMic(available: available)
        }
    }
    
    func onUserVideoAvailable(_ userId: String, available: Bool) {
        print("*** onUserAudioAvailable: userId: \(userId), available: \(available)")
        if let user = remoteUsers.first(where: { $0.userId == userId }) {
            user.updateRemoteVideo(available: available)
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

