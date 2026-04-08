import ComposeApp

protocol StateRobot {
    associatedtype State
    associatedtype Event: AnyObject

    var capturedEvents: [Event] { get }
    var lastEvent: Event? { get }
    func createEventSink() -> (Event) -> Void
    func defaultState() -> State
    func clearEvents()
}

class BaseStateRobot<State, Event: AnyObject>: StateRobot {
    private(set) var capturedEvents: [Event] = []
    var lastEvent: Event? { capturedEvents.last }

    func createEventSink() -> (Event) -> Void {
        { [weak self] event in self?.capturedEvents.append(event) }
    }

    func clearEvents() { capturedEvents.removeAll() }

    func defaultState() -> State {
        fatalError("Subclass must override defaultState()")
    }
}
