package org.techascent.aifinder.di

import org.koin.core.module.Module

/** Provides platform bindings such as [org.techascent.aifinder.data.auth.GoogleAuthDataSource]. */
expect val platformModule: Module
