Public Class Members
    Private Const Max As Integer = 10, Min = 0
    Private ReadOnly _items As New List(Of String)()
    Public WithEvents Button As Button
    Dim a, b As Integer, c As String = "c"
    Private values() As Integer, matrix(,) As Double, sized(5) As Byte, maybe? As Integer

    Public Sub New(Optional ByVal x As Integer = 1, ByRef y As String, ParamArray rest() As Object)
        MyBase.New()
    End Sub

    Public Overridable Function Compute(Of T)(value As T) As T
        Return value
    End Function

    Protected Overrides Sub OnLoad(e As EventArgs) Handles MyBase.Load, Button.Click
    End Sub

    Public Function Count() As Integer Implements ICollection.Count
        Return 0
    End Function

    Public Shared Operator +(left As Members, right As Members) As Members
        Return left
    End Operator

    Public Shared Widening Operator CType(value As Integer) As Members
        Return Nothing
    End Operator

    Public Property Name As String = "default"
    Public Property Items As New List(Of Integer) From {1, 2}

    Public Property Value As Integer
        Get
            Return _value
        End Get
        Private Set(value As Integer)
            _value = value
        End Set
    End Property

    Default Public ReadOnly Property Item(index As Integer) As String
        Get
            Return _items(index)
        End Get
    End Property

    Public Event Changed(sender As Object, e As EventArgs)

    Public Custom Event Clicked As EventHandler
        AddHandler(value As EventHandler)
        End AddHandler
        RemoveHandler(value As EventHandler)
        End RemoveHandler
        RaiseEvent(sender As Object, e As EventArgs)
        End RaiseEvent
    End Event

    Declare Auto Function MessageBox Lib "user32.dll" Alias "MessageBoxA" (hWnd As IntPtr, text As String) As Integer

    Public MustOverride Sub Abstract()

    Async Function LoadAsync() As Task(Of Integer)
        Dim result = Await Task.FromResult(1)
        Return result
    End Function

    Iterator Function Numbers() As IEnumerable(Of Integer)
        Yield 1
        Yield 2
    End Function
End Class
