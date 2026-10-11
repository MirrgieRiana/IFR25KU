package miragefairy2024.util

import net.minecraft.world.phys.Vec3
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * 射出された飛び道具の弾道を決めるパラメータなのだ～🌱
 * [inertia] は毎 tick 速度へ掛かる慣性で、[gravity] は毎 tick y の速度から引かれる重力なのだ～🌱
 */
data class Trajectory(val initialVelocity: Vec3, val inertia: Double, val gravity: Double)

/**
 * 弾道の上の 1 点なのだ～🌱
 * [offset] は射出位置からの相対位置で、[ticks] はそこへ到達するまでの tick 数なのだ～🌱
 */
data class TrajectoryPoint(val offset: Vec3, val ticks: Double)

/**
 * 射出された飛び道具が、射出位置からの水平距離が [horizontalDistance] になった時点で居る、弾道の上の点なのだ～🌱
 * その水平距離まで届かない場合と、真上や真下に近い向きで撃たれて水平方向にほとんど進まない場合は、null を返すのだ～🌱
 *
 * [net.minecraft.world.entity.projectile.AbstractArrow.tick] は、旧速度で位置を進めてから、速度に慣性を掛けて重力を引くのだ～🌱
 * すると各軸の速度が等比数列になるから、その和として、n tick 後の位置を閉じた式で書けるのだ～🌱
 * 慣性を無視した初速と重力加速度だけの放物線は、20 tick の時点で既に 5 ブロック以上ずれるから、慣性を含めた式でなければならないのだ～🌱
 */
fun Trajectory.getPointAtHorizontalDistance(horizontalDistance: Double): TrajectoryPoint? {
    val horizontalSpeed = sqrt(initialVelocity.x * initialVelocity.x + initialVelocity.z * initialVelocity.z)
    if (horizontalSpeed < 1.0e-6) return null

    // 慣性の累乗 k^n は、水平距離が等比数列の和であることから逆算できるのだ～🌱
    val inertiaPower = 1.0 - horizontalDistance * (1.0 - inertia) / horizontalSpeed
    if (inertiaPower <= 0.0) return null // 水平方向の到達距離には上限があって、そこへ届かない場合なのだ～🌱
    val ticks = ln(inertiaPower) / ln(inertia)

    val velocitySum = horizontalDistance / horizontalSpeed // = (1 - k^n) / (1 - k)
    val y = initialVelocity.y * velocitySum - gravity / (1.0 - inertia) * (ticks - velocitySum)
    return TrajectoryPoint(Vec3(initialVelocity.x / horizontalSpeed * horizontalDistance, y, initialVelocity.z / horizontalSpeed * horizontalDistance), ticks)
}
