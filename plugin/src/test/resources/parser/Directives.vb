#Region "R"
Imports System
#End Region
#If DEBUG Then
Module M
#Else
Module N
#End If
    ' comment
    Rem rem comment
    ''' <summary>Doc</summary>
    Sub Main() ' trailing
        x = 1 + _
            2
    End Sub
End Module
