<Serializable>
Public MustInherit Class Base(Of T As {Class, New}, In U, Out V)
    Inherits Object
    Implements IDisposable, IEnumerable(Of T)
End Class

Friend Structure Point
    Public X As Integer
End Structure

Module Program
End Module

Public Interface IShape
    Sub Draw()
    Function Area() As Double
    Property Name As String
    ReadOnly Property Id(index As Integer) As Integer
    Event Changed As EventHandler
End Interface

Public Enum Color As Byte
    Red = 1
    <Obsolete> Green
    Blue
End Enum

Public Delegate Function Transformer(Of T)(value As T) As T
