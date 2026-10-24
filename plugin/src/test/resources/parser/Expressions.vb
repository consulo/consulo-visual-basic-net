Module Expressions
    Sub Main()
        x = a + b * c ^ -d Mod e \ f & "s"
        x = Not a = b AndAlso c OrElse d Xor e
        x = a Is Nothing OrElse a IsNot b OrElse a Like "*"
        x = a << 2 >> 1
        x = TypeOf a Is String
        x = If(a, b, c) + If(a, b)
        x = CType(a, Integer) + DirectCast(a, String).Length + TryCast(a, Object)
        x = CInt(a) + CStr(b)
        x = GetType(List(Of )) Is GetType(Dictionary(Of String, Integer))
        x = NameOf(Main)
        x = New Point With {.X = 1, .Y = 2}
        x = New With {Key .Id = 1, .Name = "n"}
        x = New Integer() {1, 2, 3}
        x = New Integer(9) {}
        x = New List(Of Integer) From {1, 2}
        x = {1, 2, {3, 4}}
        x = (1, 2)
        x = (a:=1, b:=2)
        x = obj?.Name?.Length
        x = arr?(0)
        x = dict!key
        x = Me.Value + MyBase.Value + MyClass.Value
        x = Global.System.Math.Max(1, 2)
        x = Integer.MaxValue + String.Empty.Length
        x = F(Of Integer)(1, , name:=2)
        x = #1/1/2000# + 1.5D + &HFF + "c"c
        x = $"Hello {name,10:N2}!"
        x = Function(a) a + 1
        x = Function(a As Integer) As Integer
                Return a
            End Function
        x = Sub() Console.WriteLine()
        x = Async Function() Await Task.Delay(1)
        x = a +
            b
        x = Foo(
            1,
            2
        )
        x = Console.Error
    End Sub
End Module
