//
//  UniThenApp.swift
//  UniThen
//
//  Created by Moritz on 24.07.26.
//

import unithen
import SwiftUI

@main
struct UniThenApp: App {
#if
DEBUG
UniThenKt.DebugApplication()
#else
UniThenKt.Application()
#endif

    var body: some Scene {
        WindowGroup {
            ComposeViewController().ignoresSafeArea(.all)
        }
    }
}
