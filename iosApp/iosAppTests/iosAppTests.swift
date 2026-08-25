import XCTest
@testable import iosApp

final class iosAppTests: XCTestCase {

    func testInfoItem() throws {
        let item = InfoItem(label: "Test", value: "Value")
        XCTAssertEqual(item.label, "Test")
        XCTAssertEqual(item.value, "Value")
        XCTAssertEqual(item.id, "Test-Value")
    }

    func testPerformanceExample() throws {
        self.measure {
            // Put the code you want to measure the time of here.
        }
    }
}
