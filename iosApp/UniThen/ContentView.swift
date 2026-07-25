//
//  ContentView.swift
//  UniThen
//
//  Created by Moritz on 24.07.26.
//

import shared
import SwiftUI

struct ComposeViewController: UIViewControllerRepresentable {
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
    
    func makeUIViewController(context: Context) -> UIViewController {
        return IosMainActivityKt.IosMainActivity()
    }
}
