package co.upvest.google4s
import cats.~>
import com.google.api.gax.core.{
  GoogleCredentialsProvider,
  NoCredentialsProvider,
  CredentialsProvider => GCredentialsProvider
}

package object core {

  object IdLift            extends Lifts.IdInstance
  object FutureLift        extends Lifts.FutureInstances
  object TryLift           extends Lifts.TryInstances
  object GoogleConversions extends GoogleUtils.GoogleConverters

  // Lazy lift. A natural transformation
  // explicitly lazy, to deal with
  // data types like Future.
  final type Llift[F[_]] = (() => ?) ~> F

  final case class Project(s: String) extends AnyVal

  /**
   * Mostly needed for testing ot pointing the config to
   * a special target. Otherwise the target is resolved
   * automatically.
   * @param s in `host:port` format.
   */
  final case class HostPort(s: String) extends AnyVal

  /**
   * Wrapper over api.gax.core.CredentialsProvider
   * @param cp CredentialsProvider.
   */
  final case class Credentials(cp: GCredentialsProvider)

  object Credentials {

    implicit def unwrap(c: Credentials): GCredentialsProvider =
      c.cp

    /**
     * Used for Tests or dedicated instances.
     * @return NoCredentials provider
     */
    def noCredentials: Credentials =
      Credentials(NoCredentialsProvider.create)

    /**
     * Uses the credentials stored in GOOGLE_APPLICATION_CREDENTIALS
     * environment variable.
     * @return GoogleCredentialsProvider
     */
    def defaultCredentials: Credentials =
      Credentials(GoogleCredentialsProvider.newBuilder.build)

    /**
     * Custom credentials provider.
     * @param cp Credentials provider instance.
     * @return Wrapped custom credentials provider.
     */
    def fromGoogleCredentials(cp: GCredentialsProvider): Credentials =
      Credentials(cp)
  }
}
