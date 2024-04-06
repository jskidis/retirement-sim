package medical

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize

class MedicarePremiumBracketsTest : ShouldSpec({

    should("loadBrackets") {
        val results = MedicarePremiumBrackets.loadBrackets()
        results.shouldHaveSize(6)
    }
})

