#Const Feature = True
#Const Level = 2
Module M
#If Feature AndAlso Level >= 2 Then
    Sub Active()
    End Sub
#ElseIf Feature Then
    Sub NotTaken()
        x = "unbalanced
    End Sub
#Else
    Sub Inactive()
    End Sub
#End If
#If Not Feature Then
    Sub Disabled()
#If True Then
    Sub Nested()
#End If
#Else
    Sub Enabled()
    End Sub
#End If
#If UNDEFINED Then
    Sub Undefined()
    End Sub
#End If
End Module
