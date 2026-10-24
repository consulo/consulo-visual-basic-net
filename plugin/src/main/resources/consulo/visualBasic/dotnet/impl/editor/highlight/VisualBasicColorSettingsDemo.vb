Imports System.Runtime.CompilerServices

#Const TRACE = True

Namespace Demo
    ''' <summary>Shapes and their reports.</summary>
    Public Interface <interface_name>IShape</interface_name>
        ReadOnly Property <instance_property>Area</instance_property> As Double
    End Interface

    Public Enum <enum_name>Kind</enum_name>
        <constant>Circle</constant>
        <constant>Square</constant>
    End Enum

    Public Structure <structure_name>Point</structure_name>
        Public <instance_field>X</instance_field>, <instance_field>Y</instance_field> As Integer
    End Structure

    Public Delegate Sub <delegate_name>Notify</delegate_name>(<parameter>message</parameter> As String)

    <<attribute_name>Serializable</attribute_name>>
    Public Class <class_name>Shape</class_name>(Of <generic_parameter_name>T</generic_parameter_name>)
        Implements <interface_name>IShape</interface_name>

        Public Const <constant>Sides</constant> As Integer = 4
        Public Shared <static_field>Count</static_field> As Integer
        Public Shared Property <static_property>Unit</static_property> As <class_name>Shape</class_name>(Of <generic_parameter_name>T</generic_parameter_name>)
        Public Event <instance_event>Changed</instance_event> As <delegate_name>EventHandler</delegate_name>
        Public Shared Event <static_event>Created</static_event> As <delegate_name>EventHandler</delegate_name>

        Public ReadOnly Property <instance_property>Area</instance_property> As Double Implements <interface_name>IShape</interface_name>.<instance_property>Area</instance_property>

        Public <contextual_keyword>Async</contextual_keyword> Function <method_name>DescribeAsync</method_name>(<parameter>kind</parameter> As <enum_name>Kind</enum_name>) As Task(Of String)
            Dim <local_var>created</local_var> = #2026-10-24#
            Dim <local_var>text</local_var> = $"{<parameter>kind</parameter>} of {<constant>Sides</constant>} sides, " &
                      <class_name>Math</class_name>.<static_call>Round</static_call>(<instance_property>Area</instance_property> * 2.5, 2)
            <static_field>Count</static_field> += 1
            <contextual_keyword>Await</contextual_keyword> <class_name>Task</class_name>.<static_call>Delay</static_call>(10)
#If TRACE Then
            <class_name>Console</class_name>.<static_call>WriteLine</static_call>(<local_var>text</local_var>.<instance_call>ToUpper</instance_call>())
#Else
<disabled>            Console.WriteLine("release")
</disabled>#End If
            Dim <local_var>xml</local_var> = <shape kind=<%= <parameter>kind</parameter> %>><!-- area -->square</shape>
            RaiseEvent <instance_event>Changed</instance_event>(Me, <class_name>EventArgs</class_name>.<static_field>Empty</static_field>)
            Return <local_var>text</local_var> ' a comment
        End Function
    End Class

    Public Module <module_name>Extensions</module_name>
        <<attribute_name>Extension</attribute_name>>
        Public Function <method_name>Twice</method_name>(<parameter>value</parameter> As Integer) As Integer
            Return <parameter>value</parameter> * 2
        End Function
    End Module
End Namespace
