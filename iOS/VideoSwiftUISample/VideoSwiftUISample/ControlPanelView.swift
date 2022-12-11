//
//  ControlPanelView.swift
//  VideoSwiftUISample
//

import SwiftUI

struct ControlPanelView: View {
    @EnvironmentObject var viewModel: TrtcViewModel
    
    var body: some View {
        VStack {
            StatusView()
            ControlView()
            ErrorView()
        }
        .padding()
    }
    
    struct StatusView: View {
        @EnvironmentObject var viewModel: TrtcViewModel
        
        var body: some View {
            HStack {
                Label(viewModel.userId, systemImage: "person")
                //Spacer()
                Text("Room: #\(viewModel.roomId) (+\(viewModel.remoteUsers.count) Joined)")
            }
        }
    }
    
    struct ControlView: View {
        @EnvironmentObject var viewModel: TrtcViewModel

        var body: some View {
            HStack {
                Spacer()

                Button(action: {
                    if viewModel.videoAvailable {
                        viewModel.muteLocalVideo(mute: true)
                    } else {
                        viewModel.muteLocalVideo(mute: false)
                    }
                }, label: {
                    Image(systemName: viewModel.videoAvailable ? "video.fill" : "video.slash.fill")
                })
                .tint(.primary)
                .padding(.trailing)

                Button(action: {
                    viewModel.switchCamera()
                }, label: {
                    Image(systemName: "arrow.triangle.2.circlepath")
                })
                .disabled(!viewModel.videoAvailable)
                .tint(.primary)
                .padding(.trailing)
                
                Button(action: {
                    if viewModel.audioAvailable {
                        viewModel.muteLocalAudio(mute: true)
                    } else {
                        viewModel.muteLocalAudio(mute: false)
                    }
                }, label: {
                    Image(systemName: viewModel.audioAvailable ? "mic.fill" : "mic.slash.fill")
                })
                .tint(.primary)
                .padding(.trailing)
                
                Button(action: {
                    if viewModel.joined {
                        viewModel.exitRoom()
                    } else {
                        viewModel.enterRoom()
                    }
                }) {
                    HStack {
                        Image(systemName: "phone.fill")
                        Text(viewModel.joined ? "Leave" : "Join")
                    }
                }
                .buttonStyle(.borderedProminent)
                .tint(viewModel.joined ? .red : .green)
            }
        }
    }

    struct ErrorView: View {
        @EnvironmentObject var viewModel: TrtcViewModel
        
        var body: some View {
            HStack {
                if viewModel.errCode != 0 {
                    Text(viewModel.errMsg)
                }
            }
        }
    }
}

struct ControlPanelView_Previews: PreviewProvider {
    static var previews: some View {
        let viewModel = TrtcViewModel(debugPreview: true)

        ZStack(alignment: .topTrailing) {
            VStack {
                Spacer()
                    .frame(maxHeight: .infinity)
                ControlPanelView()
                    .background(Color.black.opacity(0.3))
                    .cornerRadius(8)
                    .environmentObject(viewModel)
            }
            .padding()
        }
    }
}
