package medical

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize

class MedicarePremiumBracketsTest : FunSpec({

    test("loadBrackets") {
        val results = MedicarePremiumBrackets.loadBrackets()
        results.shouldHaveSize(6)
    }
})

