//
//  RemoteUser.swift
//  VideoSample2
//

import Foundation
import UIKit
import TXLiteAVSDK_TRTC

class RemoteUser {
    var userId: String
    var videoAvailable: Bool
    var audioAvailable: Bool
    var videoView: UIView!
    var micImage: UIImageView!
    var userIdLabel: UILabel!
    private var trtcCloud: TRTCCloud = TRTCCloud.sharedInstance()

    init(userId: String, videoAvailable: Bool = false, audioAvailable: Bool = false) {
        self.userId = userId
        self.videoAvailable = videoAvailable
        self.audioAvailable = audioAvailable
    }
    
    func updateRemoteMic(available: Bool) {
        audioAvailable = available
        if let micImage = micImage {
            micImage.image = UIImage(systemName:  available ? "mic.fill" : "mic.slash.fill")
        }
    }
    
    func updateRemoteVideo(available: Bool) {
        videoAvailable = available
        if let videoView = videoView {
            if available {
                trtcCloud.startRemoteView(userId, streamType:.small, view: videoView)
                videoView.layer.opacity = 1
            } else {
                trtcCloud.stopRemoteView(userId, streamType: .small)
                videoView.layer.opacity = 0.3
            }
        }
    }
}
