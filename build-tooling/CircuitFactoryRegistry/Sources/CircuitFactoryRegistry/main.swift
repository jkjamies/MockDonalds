import CircuitFactoryRegistryCore
import Foundation

/// Thin CLI wrapper: parses arguments, hands work off to
/// `CircuitFactoryRegistryCore`, writes the result to disk. Invoked by the
/// `CircuitFactoryRegistryPlugin` build tool plugin.
struct CLI {
    static func run() {
        var outputPath: String?
        var inputPaths: [String] = []

        var iterator = CommandLine.arguments.dropFirst().makeIterator()
        while let arg = iterator.next() {
            switch arg {
            case "--output":
                outputPath = iterator.next()
            default:
                inputPaths.append(arg)
            }
        }

        guard let outputPath else {
            FileHandle.standardError.write(Data("error: --output <path> is required\n".utf8))
            exit(1)
        }

        let generated = RegistryGenerator.generate(sourcePaths: inputPaths)
        do {
            try generated.write(toFile: outputPath, atomically: true, encoding: .utf8)
        } catch {
            FileHandle.standardError.write(Data("error: \(error)\n".utf8))
            exit(1)
        }
    }
}

CLI.run()
