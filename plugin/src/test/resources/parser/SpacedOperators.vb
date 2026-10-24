Module Ops
    Sub Main()
        b = a > = c
        b = a < > c
        b = a <= c Or a >= c Or a <> c
        x = a << 2 >> 1
        x <<= 1
        x > >= 1
        Select Case x
            Case Is >= 1, < 0
        End Select
    End Sub

    Public Shared Operator <>(a As Ops, b As Ops) As Boolean
        Return False
    End Operator
End Module
