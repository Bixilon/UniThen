//
//  UniThenApp.swift
//  UniThen
//
//  Created by Moritz on 24.07.26.
//

import SwiftUI

@main
struct UniThenApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeViewController().ignoresSafeArea(.all)
        }
    }
}
