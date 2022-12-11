//
//  ContentView.swift
//  VideoSwiftUISample
//

import SwiftUI

struct ContentView: View {
    let viewModel = TrtcViewModel()
    var body: some View {
        MainView()
            .environmentObject(viewModel)
    }
}

struct MainView: View {
    @EnvironmentObject var viewModel: TrtcViewModel
    var body: some View {
        ZStack(alignment: .topTrailing) {
            TrtcLocalVideoView(available: viewModel.videoAvailable, isFrontCamera: viewModel.isFrontCamera)
                .edgesIgnoringSafeArea(.all) // Expand the display area to the safe area
            VStack {
                HStack {
                    Spacer()
                        .frame(maxHeight: .infinity)
                    RemoteUserView()
                        .frame(maxHeight: .infinity, alignment: .topTrailing)
                }
                ControlPanelView()
                    .background(Color.white.opacity(0.3))
                    .cornerRadius(8)
            }
            .padding()
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        let viewModel = TrtcViewModel(debugPreview: true)
        MainView()
            .environmentObject(viewModel)
    }
}

