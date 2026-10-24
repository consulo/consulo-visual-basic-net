Module Statements
    Sub Main()
        Dim x As Integer = 1
        Static counter As Integer
        Const name = "n"
        x = 2
        x += 1 : x -= 1
        Console.WriteLine(x)
        Call Foo()
        If x > 1 Then
            x = 1
        ElseIf x < 0 Then
            x = 0
        Else
            x = -1
        End If
        If x = 1 Then x = 2 Else x = 3
        If x = 1 Then Return
        Select Case x
            Case 1, 2 To 4
                Exit Select
            Case Is > 10, < 0
            Case Else
        End Select
        While x < 10
            x += 1
        End While
        Do While x > 0
            x -= 1
            Continue Do
        Loop
        Do
        Loop Until x = 0
        For i As Integer = 0 To 10 Step 2
            Exit For
        Next
        For Each item In list
        Next item
        Try
            Throw New Exception()
        Catch ex As ArgumentException When ex.Message IsNot Nothing
        Catch
        Finally
        End Try
        Using stream As New MemoryStream(), reader = New StreamReader(stream)
        End Using
        SyncLock Me
        End SyncLock
        With obj
            .Name = "x"
            .Run()
        End With
        ReDim Preserve values(10)
        Erase values
        AddHandler Button.Click, AddressOf OnClick
        RemoveHandler Button.Click, AddressOf OnClick
        RaiseEvent Changed(Me, EventArgs.Empty)
        On Error GoTo Handler
        On Error Resume Next
Handler:
        Resume Next
        GoTo Handler
        Stop
        End
    End Sub
End Module
