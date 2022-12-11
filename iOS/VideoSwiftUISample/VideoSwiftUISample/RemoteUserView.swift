//
//  RemoteUserView.swift
//  VideoSwiftUISample
//

import SwiftUI

struct RemoteUserView: View {
    @EnvironmentObject var viewModel: TrtcViewModel
    
    var body: some View {
        VStack {
            ForEach(viewModel.remoteUsers) { remoteUser in
                ZStack(alignment: .bottomLeading) {
                    RemoteVideoView(remoteUser: remoteUser)
                        .frame(width: 100, height: 150)
                        .background(Color.black.opacity(0.3))
                    CaptionView(remoteUser: remoteUser)
                }
            }
        }
    }
    
    struct RemoteVideoView: View {
        @ObservedObject var remoteUser: RemoteUser
        
        var body: some View {
            if remoteUser.videoAvailable {
                TRTCRemoteVideoView(remoteUserId: remoteUser.userId)
            } else {
                Image(systemName: "video.slash.fill")
            }
        }
    }

    struct CaptionView: View {
        @ObservedObject var remoteUser: RemoteUser
        
        var body: some View {
            HStack(spacing: 2) {
                Image(systemName: remoteUser.audioAvailable ? "mic.fill" : "mic.slash.fill")
                    .font(.caption)
                Text(remoteUser.userId)
                    .font(.caption)
            }
            .padding(4)
        }
    }
}

struct RemoteUserView_Previews: PreviewProvider {
    static var previews: some View {
        let model = TrtcViewModel(debugPreview: true)
        
        ZStack(alignment: .topTrailing) {
            VStack {
                HStack {
                    Spacer()
                        .frame(maxHeight: .infinity)
                    RemoteUserView()
                        .frame(maxHeight: .infinity, alignment: .topTrailing)
                        .environmentObject(model)
                }
            }
            .padding()
        }
   }
}
