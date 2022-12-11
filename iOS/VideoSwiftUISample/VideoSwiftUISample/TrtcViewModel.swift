//
//  RemoteUser.swift
//  VideoSwiftUISample
//

import Foundation
import TXLiteAVSDK_TRTC

class RemoteUser: Identifiable, ObservableObject {
    var id: UUID
    var userId: String
    @Published var videoAvailable: Bool
    @Published var audioAvailable: Bool
    
    init(id: UUID = UUID(), userId: String, videoAvailable: Bool = false, audioAvailable: Bool = false) {
        self.id = id
        self.userId = userId
        self.videoAvailable = videoAvailable
        self.audioAvailable = audioAvailable
    }
}

class TrtcViewModel: NSObject, ObservableObject {
    @Published var remoteUsers: [RemoteUser] = []
    @Published var errCode: Int32 = 0
    @Published var errMsg = ""
    @Published var joined = false
    @Published var userId : String = ""
    @Published var roomId : Int = 1
    @Published var audioAvailable = true
    @Published var videoAvailable = true
    @Published var isFrontCamera = true
    
    private var trtcCloud: TRTCCloud = TRTCCloud.sharedInstance()
    private var maxRemoteUserCount = 3
    
    init(debugPreview: Bool = false) {
        super.init()
        
        trtcCloud.delegate = self
        userId = "User-" + generator(4)
        
        if debugPreview {
            for i in 1..<4 {
                let user = RemoteUser(userId: "user\(i)")
                remoteUsers.append(user)
            }
        }
    }
    
    func enterRoom() {
        errCode = 0
        errMsg = ""
        
        // stop the video once
        videoAvailable = false
        trtcCloud.stopLocalPreview()
        
        let params = TRTCParams()
        params.sdkAppId = UInt32(SDKAPPID)
        params.roomId = UInt32(roomId)
        params.userId = userId
        params.role = .anchor
        params.userSig = TrtcUserSig.genTestUserSig(identifier: userId) as String
        trtcCloud.enterRoom(params, appScene: .videoCall)
        trtcCloud.startLocalAudio(.music)
    }
    
    func exitRoom() {
        trtcCloud.stopLocalAudio()
        //trtcCloud.stopLocalPreview()
        trtcCloud.exitRoom()
    }
    
    func muteLocalAudio(mute: Bool) {
        trtcCloud.muteLocalAudio(mute)
        audioAvailable = !mute
    }
    
    func muteLocalVideo(mute: Bool) {
        trtcCloud.muteLocalVideo(.big, mute: mute)
        videoAvailable = !mute
    }
    
    func switchCamera(_ isFrontCamera: Bool? = nil) {
        if let isFrontCamera = isFrontCamera {
            self.isFrontCamera = isFrontCamera
        } else {
            self.isFrontCamera.toggle()
        }
        trtcCloud.getDeviceManager().switchCamera(self.isFrontCamera)
    }

    private func generator(_ length: Int) -> String {
        let letters = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
        var randomString = ""
        for _ in 0 ..< length {
            randomString += String(letters.randomElement()!)
        }
        return randomString
    }
}

extension TrtcViewModel: TRTCCloudDelegate {
    func onEnterRoom(_ result: Int) {
        print("*** onEnterRoom: result: \(result)")
        joined = true
        videoAvailable = true
    }
    
    func onExitRoom(_ reason: Int) {
        print("*** onExitRoom: reason: \(reason)")
        joined = false
        remoteUsers.removeAll()
    }
    
    func onRemoteUserEnterRoom(_ userId: String) {
        print("*** onRemoteUserEnterRoom: userId: \(userId)")
        if remoteUsers.count >= maxRemoteUserCount {
            return // TODO: Allow flexibility in number of participants
        }
        let remoteUser = RemoteUser(userId: userId)
        remoteUsers.append(remoteUser)
    }

    func onRemoteUserLeaveRoom(_ userId: String, reason: Int) {
        print("*** onRemoteUserLeaveRoom: userId: \(userId), reason: \(reason)")
        remoteUsers.removeAll(where: { $0.userId == userId })
    }
    
    func onUserAudioAvailable(_ userId: String, available: Bool) {
        print("*** onUserAudioAvailable: userId: \(userId): available: \(available)")
        if let remoteUser = remoteUsers.first(where: { $0.userId == userId }) {
            remoteUser.audioAvailable = available
        }
    }
    
    func onUserVideoAvailable(_ userId: String, available: Bool) {
        print("*** onUserVideoAvailable: userId: \(userId): available: \(available)")
        if let remoteUser = remoteUsers.first(where: { $0.userId == userId }) {
            remoteUser.videoAvailable = available
        }
    }
    
    func onError(_ errCode: TXLiteAVError, errMsg: String?, extInfo: [AnyHashable : Any]?) {
        self.errCode = errCode.rawValue
        self.errMsg = ""
        if let errMsg = errMsg {
            self.errMsg = errMsg
        }
        print("*** onError: \(self.errCode): \(self.errMsg)")
    }
}
