package ge.zgharbi.study.fps
package ch.c06

opaque type State[S, +A] = S => (A, S)

object State {
  def apply[A, S](f: S => (A, S)): State[S, A] = f
  
  extension [S, A](self: State[S, A]) def run(s: S): (A, S) = self(s)
}
