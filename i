func main()
v0 = 4
vmt_A = alloc(v0)
v1 = @A_Upon
[vmt_A + 0] = v1
v2 = 8
vmt_BT = alloc(v2)
v3 = @BT_Start
[vmt_BT + 0] = v3
v4 = @BT_End
[vmt_BT + 4] = v4
v5 = 0
vmt_Test = alloc(v5)
v7 = 12
v6 = alloc(v7)
v8 = 0
[v6 + 4] = v8
v9 = 0
[v6 + 8] = v9
[v6 + 0] = vmt_BT
v14 = 12
v13 = alloc(v14)
v15 = 0
[v13 + 4] = v15
v16 = 0
[v13 + 8] = v16
[v13 + 0] = vmt_BT
v12 = [v6 + 0]
v10 = [v12 + 0]
v11 = call v10(v6)
print(v11)
v17 = 0
      return v17

func BT_Start(this)
v22 = 4
vmt_A = alloc(v22)
v23 = @A_Upon
[vmt_A + 0] = v23
v24 = 8
vmt_BT = alloc(v24)
v25 = @BT_Start
[vmt_BT + 0] = v25
v26 = @BT_End
[vmt_BT + 4] = v26
v27 = 0
vmt_Test = alloc(v27)
v28 = 0
v28 = 0
v29 = 1
v28 = v29
v30 = 0
[this + 8] = v30
if0 v28 goto end_0
if0 v19 goto end_3
v32 = 0
goto end_5
end_3:
v32 = 1
goto end_5
end_5:
if0 v28 goto end_0
goto end_1
end_0:
v31 = 0
goto end_2
end_1:
v31 = 1
end_2:
if0 v31 goto else_0
v33 = 9
print(v33)
goto end_6
else_0:
v34 = 8
print(v34)
end_6:
v35 = 0
      return v35

func BT_End(this)
v36 = 4
vmt_A = alloc(v36)
v37 = @A_Upon
[vmt_A + 0] = v37
v38 = 8
vmt_BT = alloc(v38)
v39 = @BT_Start
[vmt_BT + 0] = v39
v40 = @BT_End
[vmt_BT + 4] = v40
v41 = 0
vmt_Test = alloc(v41)
v42 = 9
      return v42

func A_Upon(this param0)
v43 = 4
vmt_A = alloc(v43)
v44 = @A_Upon
[vmt_A + 0] = v44
v45 = 8
vmt_BT = alloc(v45)
v46 = @BT_Start
[vmt_BT + 0] = v46
v47 = @BT_End
[vmt_BT + 4] = v47
v48 = 0
vmt_Test = alloc(v48)
      return param0


