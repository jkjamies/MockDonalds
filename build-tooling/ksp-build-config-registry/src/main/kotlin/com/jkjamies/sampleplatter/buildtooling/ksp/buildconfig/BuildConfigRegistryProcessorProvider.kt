package com.jkjamies.sampleplatter.buildtooling.ksp.buildconfig

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class BuildConfigRegistryProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        BuildConfigRegistryProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
        )
}
