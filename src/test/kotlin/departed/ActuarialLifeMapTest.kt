package departed

import config.ActuarialGender
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeLessThan

class ActuarialLifeMapTest : ShouldSpec({

    should("should have an entry female pct should be less than male pct until at least age 100") {
        (0..100).forEach {
            ActuarialLifeMap.getChanceOfEvent(it, ActuarialGender.FEMALE).shouldBeLessThan(
                ActuarialLifeMap.getChanceOfEvent(it, ActuarialGender.MALE))
        }
    }

    should("chances should increase with age from at least age 16 onwards") {
        (15..120).forEach {
            ActuarialLifeMap.getChanceOfEvent(it -1, ActuarialGender.MALE).shouldBeLessThan(
                ActuarialLifeMap.getChanceOfEvent(it, ActuarialGender.MALE))

            ActuarialLifeMap.getChanceOfEvent(it -1, ActuarialGender.FEMALE).shouldBeLessThan(
                ActuarialLifeMap.getChanceOfEvent(it, ActuarialGender.FEMALE))
        }
    }
})
