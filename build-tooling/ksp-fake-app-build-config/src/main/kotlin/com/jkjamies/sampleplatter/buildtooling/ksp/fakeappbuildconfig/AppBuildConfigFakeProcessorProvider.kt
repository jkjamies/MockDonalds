package com.jkjamies.sampleplatter.buildtooling.ksp.fakeappbuildconfig

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class AppBuildConfigFakeProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        AppBuildConfigFakeProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
        )
}
