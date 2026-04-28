import CircuitFactoryRegistryCore
import Foundation

/// Thin CLI wrapper: parses arguments, hands work off to
/// `CircuitFactoryRegistryCore`, writes the result to disk. Invoked by the
/// `Generate Circuit factory registry` Run Script Phase.
///
/// Inputs may be passed as positional arguments OR via `--paths-from-stdin`,
/// which reads NUL-separated paths from standard input (matches
/// `find -print0`). Stdin sidesteps the system `ARG_MAX` limit — important
/// because piping `find | xargs` would fan out into multiple invocations once
/// the list grows large enough, and each invocation truncates the output
/// file, silently dropping earlier entries.
struct CLI {
    static func run() {
        var outputPath: String?
        var inputPaths: [String] = []
        var pathsFromStdin = false

        var iterator = CommandLine.arguments.dropFirst().makeIterator()
        while let arg = iterator.next() {
            switch arg {
            case "--output":
                outputPath = iterator.next()
            case "--paths-from-stdin":
                pathsFromStdin = true
            default:
                inputPaths.append(arg)
            }
        }

        guard let outputPath else {
            FileHandle.standardError.write(Data("error: --output <path> is required\n".utf8))
            exit(1)
        }

        if pathsFromStdin {
            let data = FileHandle.standardInput.readDataToEndOfFile()
            let combined = String(decoding: data, as: UTF8.self)
            for piece in combined.split(separator: "\0", omittingEmptySubsequences: true) {
                inputPaths.append(String(piece))
            }
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
