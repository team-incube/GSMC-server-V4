package team.incube.gsmc.domain.alert

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class AlertTest :
    BehaviorSpec({
        Given("점수 승인 알림을 생성할 때") {
            When("Alert.approved를 호출하면") {
                Then("APPROVED 타입에 카테고리명이 포함된 미읽음 알림이 만들어진다") {
                    val alert = Alert.approved(userId = 10L, scoreId = 1L, categoryName = "수상경력")

                    alert.userId shouldBe 10L
                    alert.scoreId shouldBe 1L
                    alert.alertType shouldBe AlertType.APPROVED
                    alert.isRead shouldBe false
                    alert.content shouldBe "수상경력 점수가 승인되었습니다."
                }
            }
        }

        Given("점수 거절 알림을 생성할 때") {
            When("Alert.rejected를 호출하면") {
                Then("REJECTED 타입에 거절 사유가 포함된 미읽음 알림이 만들어진다") {
                    val alert =
                        Alert.rejected(
                            userId = 10L,
                            scoreId = 1L,
                            categoryName = "수상경력",
                            rejectionReason = "증빙자료가 부족합니다",
                        )

                    alert.userId shouldBe 10L
                    alert.scoreId shouldBe 1L
                    alert.alertType shouldBe AlertType.REJECTED
                    alert.isRead shouldBe false
                    alert.content shouldBe "수상경력 점수가 거절되었습니다. 사유: 증빙자료가 부족합니다"
                }
            }
        }
    })
