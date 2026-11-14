package dev.mim1q.derelict.client.render.entity.boss.arachne

import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeDeformation
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition

object ArachneTexturedModelData {
    fun create(): LayerDefinition = MeshDefinition().let {
        it.root.apply {
            addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0F, 14F, 6F)).apply {
                addOrReplaceChild("sternum", CubeListBuilder.create().texOffs(0, 46).addBox(-8F, -4.75F, -0.5F, 16F, 10F, 24F), PartPose.offsetAndRotation(0F, 0F, -25F, 0.2618F, 0F, 0F)).apply {
                    addOrReplaceChild("sternumHair1_r1", CubeListBuilder.create().texOffs(-8, 88).addBox(-8F, -18.25F, -14F, 16F, 0F, 8F), PartPose.offsetAndRotation(0F, 2.25F, 35.5F, 0.6109F, 0F, 0F))
                    addOrReplaceChild("sternumHair0_r1", CubeListBuilder.create().texOffs(-8, 80).addBox(-8F, -18.25F, -14F, 16F, 0F, 8F), PartPose.offsetAndRotation(0F, 2.25F, 26.5F, 0.6109F, 0F, 0F))
                    addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(0, 0).addBox(-10F, -8.7547F, -0.6287F, 20F, 18F, 28F), PartPose.offsetAndRotation(0F, -0.7453F, 22.8787F, -0.3491F, 0F, 0F)).apply {
                        addOrReplaceChild("egg_base", CubeListBuilder.create().texOffs(0, 101).addBox(-7F, -2.4288F, -11.012F, 14F, 5F, 22F), PartPose.offset(0F, -11.326F, 13.3833F))
                        addOrReplaceChild("eggs", CubeListBuilder.create(), PartPose.offset(0F, -2.826F, 22.3833F)).apply {
                            addOrReplaceChild("egg16", CubeListBuilder.create().texOffs(0, 58).addBox(-3F, -2.9288F, -3.012F, 6F, 6F, 6F), PartPose.offset(0F, -12F, -5F))
                            addOrReplaceChild("egg15", CubeListBuilder.create().texOffs(0, 58).addBox(-3F, -2.9288F, -3.012F, 6F, 6F, 6F), PartPose.offset(8F, -6F, 1F))
                            addOrReplaceChild("egg14", CubeListBuilder.create().texOffs(0, 58).addBox(-3F, -2.9288F, -3.012F, 6F, 6F, 6F), PartPose.offset(-9F, -6F, -2F))
                            addOrReplaceChild("egg13", CubeListBuilder.create().texOffs(0, 58).addBox(-3F, -2.9288F, -3.012F, 6F, 6F, 6F), PartPose.offset(-5F, -11F, -19F))
                            addOrReplaceChild("egg12", CubeListBuilder.create().texOffs(8, 50).addBox(-2F, -1.9288F, -2.012F, 4F, 4F, 4F), PartPose.offset(-1F, -5F, -22F))
                            addOrReplaceChild("egg11", CubeListBuilder.create().texOffs(8, 50).addBox(-2F, -1.9288F, -2.012F, 4F, 4F, 4F), PartPose.offset(-7F, -5F, 4F))
                            addOrReplaceChild("egg10", CubeListBuilder.create().texOffs(8, 50).addBox(-2F, -1.9288F, -2.012F, 4F, 4F, 4F), PartPose.offset(0F, -5F, 5F))
                            addOrReplaceChild("egg9", CubeListBuilder.create().texOffs(8, 50).addBox(-2F, -1.9288F, -2.012F, 4F, 4F, 4F), PartPose.offset(6F, -11F, -3F))
                            addOrReplaceChild("egg8", CubeListBuilder.create().texOffs(8, 50).addBox(-2F, -1.9288F, -2.012F, 4F, 4F, 4F), PartPose.offset(10F, -7F, -6F))
                            addOrReplaceChild("egg7", CubeListBuilder.create().texOffs(8, 50).addBox(-2F, -1.9288F, -2.012F, 4F, 4F, 4F), PartPose.offset(-1F, -11F, 1F))
                            addOrReplaceChild("egg6", CubeListBuilder.create().texOffs(8, 50).addBox(-2F, -1.9288F, -2.012F, 4F, 4F, 4F), PartPose.offset(7F, -6F, -13F))
                            addOrReplaceChild("egg5", CubeListBuilder.create().texOffs(8, 50).addBox(-2F, -1.9288F, -2.012F, 4F, 4F, 4F), PartPose.offset(-9F, -6F, -14F))
                            addOrReplaceChild("egg4", CubeListBuilder.create().texOffs(8, 50).addBox(-2F, -1.9288F, -2.012F, 4F, 4F, 4F), PartPose.offset(-7F, -10F, -7F))
                            addOrReplaceChild("egg3", CubeListBuilder.create().texOffs(8, 50).addBox(-2F, -1.9288F, -2.012F, 4F, 4F, 4F), PartPose.offset(-1F, -11F, -13F))
                            addOrReplaceChild("egg2", CubeListBuilder.create().texOffs(0, 58).addBox(-3F, -2.9288F, -3.012F, 6F, 6F, 6F), PartPose.offset(8F, -6F, -21F))
                            addOrReplaceChild("egg1", CubeListBuilder.create().texOffs(8, 50).addBox(-2F, -1.9288F, -2.012F, 4F, 4F, 4F), PartPose.offset(4F, -12F, -19F))
                            addOrReplaceChild("egg0", CubeListBuilder.create().texOffs(0, 58).addBox(-3F, -2.9288F, -3.012F, 6F, 6F, 6F), PartPose.offset(4F, -14F, -11F))
                        }
                    }
                }
                addOrReplaceChild("head", CubeListBuilder.create().texOffs(56, 46).addBox(-6F, -4.3905F, -11.8595F, 12F, 8F, 12F), PartPose.offset(0F, -2.6095F, -23.1405F)).apply {
                    addOrReplaceChild("headHair1_r1", CubeListBuilder.create().texOffs(0, 11).addBox(-6F, -5F, 0F, 12F, 5F, 0F), PartPose.offsetAndRotation(0F, -4.3905F, -4.8595F, -0.7854F, 0F, 0F))
                    addOrReplaceChild("headHair0_r1", CubeListBuilder.create().texOffs(0, 16).addBox(-6F, -3F, 0F, 12F, 3F, 0F), PartPose.offsetAndRotation(0F, -4.3905F, -7.8595F, -0.7854F, 0F, 0F))
                    addOrReplaceChild("right_fang", CubeListBuilder.create().texOffs(14, 19).addBox(-2F, 0F, -2F, 4F, 6F, 3F), PartPose.offsetAndRotation(-3F, 0.6095F, -11.8595F, 0F, 0.4363F, 0F)).apply {
                        addOrReplaceChild("rightFangEnd_r1", CubeListBuilder.create().texOffs(8, 43).addBox(0F, -1.5F, -1.5F, 0F, 3F, 3F), PartPose.offsetAndRotation(0F, 7.5F, -0.5F, 0F, 1.0472F, 0F))
                    }
                    addOrReplaceChild("left_fang", CubeListBuilder.create().texOffs(0, 19).addBox(-2F, 0F, -2F, 4F, 6F, 3F), PartPose.offsetAndRotation(3F, 0.6095F, -11.8595F, 0F, -0.4363F, 0F)).apply {
                        addOrReplaceChild("leftFangEnd_r1", CubeListBuilder.create().texOffs(11, 16).addBox(0F, -1.5F, -1.5F, 0F, 3F, 3F), PartPose.offsetAndRotation(0F, 7.5F, -0.5F, 0F, -1.0472F, 0F))
                    }
                }
                addOrReplaceChild("left_leg_joint0", CubeListBuilder.create(), PartPose.offset(8F, -3F, -21F)).apply {
                    addOrReplaceChild("left_leg0", CubeListBuilder.create().texOffs(68, 8).addBox(-2F, -2F, -2F, 24F, 4F, 4F, CubeDeformation(0.1F)).texOffs(56, 66).addBox(-2F, -5F, 0F, 24F, 3F, 0F).texOffs(68, 18).addBox(-2F, 2F, 0F, 24F, 1F, 0F).texOffs(0, 0).addBox(15F, -2.5F, -2.5F, 7F, 6F, 5F, CubeDeformation(0.25F)).texOffs(0, 46).addBox(22F, -3.5F, 0F, 4F, 8F, 0F), PartPose.ZERO).apply {
                        addOrReplaceChild("left_leg_front0", CubeListBuilder.create().texOffs(68, 0).addBox(0F, -2F, -2F, 24F, 4F, 4F).texOffs(68, 16).addBox(0F, -4F, -0.1F, 24F, 2F, 0F).texOffs(68, 19).addBox(0F, 2F, 0F, 24F, 1F, 0F).texOffs(19, 0).addBox(24F, -2F, 0F, 4F, 4F, 0F), PartPose.offset(20F, 0F, 0F))
                    }
                }
                addOrReplaceChild("left_leg_joint1", CubeListBuilder.create(), PartPose.offset(8F, -5F, -15F)).apply {
                    addOrReplaceChild("left_leg1", CubeListBuilder.create().texOffs(68, 8).addBox(-2F, -2F, -2F, 24F, 4F, 4F, CubeDeformation(0.1F)).texOffs(56, 66).addBox(-2F, -5F, 0F, 24F, 3F, 0F).texOffs(68, 18).addBox(-2F, 2F, 0F, 24F, 1F, 0F).texOffs(0, 0).addBox(15F, -2.5F, -2.5F, 7F, 6F, 5F, CubeDeformation(0.25F)).texOffs(0, 46).addBox(22F, -3.5F, 0F, 4F, 8F, 0F), PartPose.ZERO).apply {
                        addOrReplaceChild("left_leg_front1", CubeListBuilder.create().texOffs(68, 0).addBox(0F, -2F, -2F, 24F, 4F, 4F).texOffs(68, 16).addBox(0F, -4F, -0.1F, 24F, 2F, 0F).texOffs(68, 19).addBox(0F, 2F, 0F, 24F, 1F, 0F).texOffs(19, 0).addBox(24F, -2F, 0F, 4F, 4F, 0F), PartPose.offset(20F, 0F, 0F))
                    }
                }
                addOrReplaceChild("left_leg_joint2", CubeListBuilder.create(), PartPose.offset(8F, -7F, -9F)).apply {
                    addOrReplaceChild("left_leg2", CubeListBuilder.create().texOffs(68, 8).addBox(-2F, -2F, -2F, 24F, 4F, 4F, CubeDeformation(0.1F)).texOffs(56, 66).addBox(-2F, -5F, 0F, 24F, 3F, 0F).texOffs(68, 18).addBox(-2F, 2F, 0F, 24F, 1F, 0F).texOffs(0, 0).addBox(15F, -2.5F, -2.5F, 7F, 6F, 5F, CubeDeformation(0.25F)).texOffs(0, 46).addBox(22F, -3.5F, -0.25F, 4F, 8F, 0F), PartPose.ZERO).apply {
                        addOrReplaceChild("left_leg_front2", CubeListBuilder.create().texOffs(68, 0).addBox(0F, -2F, -2F, 24F, 4F, 4F).texOffs(68, 16).addBox(0F, -4F, -0.1F, 24F, 2F, 0F).texOffs(68, 19).addBox(0F, 2F, 0F, 24F, 1F, 0F).texOffs(19, 0).addBox(24F, -2F, 0F, 4F, 4F, 0F), PartPose.offset(20F, 0F, 0F))
                    }
                }
                addOrReplaceChild("left_leg_joint3", CubeListBuilder.create(), PartPose.offset(8F, -9F, -3F)).apply {
                    addOrReplaceChild("left_leg3", CubeListBuilder.create().texOffs(68, 8).addBox(-2F, -2F, -2F, 24F, 4F, 4F, CubeDeformation(0.1F)).texOffs(56, 66).addBox(-2F, -5F, 0F, 24F, 3F, 0F).texOffs(68, 18).addBox(-2F, 2F, 0F, 24F, 1F, 0F).texOffs(0, 0).addBox(15F, -2.5F, -2.5F, 7F, 6F, 5F, CubeDeformation(0.25F)).texOffs(0, 46).addBox(22F, -3.5F, 0F, 4F, 8F, 0F), PartPose.ZERO).apply {
                        addOrReplaceChild("left_leg_front3", CubeListBuilder.create().texOffs(68, 0).addBox(0F, -2F, -2F, 24F, 4F, 4F).texOffs(68, 16).addBox(0F, -4F, -0.1F, 24F, 2F, 0F).texOffs(68, 19).addBox(0F, 2F, 0F, 24F, 1F, 0F).texOffs(19, 0).addBox(24F, -2F, 0F, 4F, 4F, 0F), PartPose.offset(20F, 0F, 0F))
                    }
                }
                addOrReplaceChild("right_leg_joint0", CubeListBuilder.create(), PartPose.offset(-8F, -3F, -21F)).apply {
                    addOrReplaceChild("right_leg0", CubeListBuilder.create().texOffs(68, 8).mirror().addBox(-22F, -2F, -2F, 24F, 4F, 4F, CubeDeformation(0.1F)).texOffs(56, 66).mirror().addBox(-22F, -5F, 0F, 24F, 3F, 0F).texOffs(68, 18).mirror().addBox(-22F, 2F, 0F, 24F, 1F, 0F).texOffs(0, 0).mirror().addBox(-22F, -2.5F, -2.5F, 7F, 6F, 5F, CubeDeformation(0.25F)).texOffs(0, 46).mirror().addBox(-26F, -3.5F, 0F, 4F, 8F, 0F), PartPose.ZERO).apply {
                        addOrReplaceChild("right_leg_front0", CubeListBuilder.create().texOffs(68, 0).mirror().addBox(-24F, -2F, -2F, 24F, 4F, 4F).texOffs(68, 16).mirror().addBox(-24F, -4F, -0.1F, 24F, 2F, 0F).texOffs(68, 19).mirror().addBox(-24F, 2F, 0F, 24F, 1F, 0F).texOffs(19, 0).mirror().addBox(-28F, -2F, 0F, 4F, 4F, 0F), PartPose.offset(-20F, 0F, 0F))
                    }
                }
                addOrReplaceChild("right_leg_joint1", CubeListBuilder.create(), PartPose.offset(-8F, -5F, -15F)).apply {
                    addOrReplaceChild("right_leg1", CubeListBuilder.create().texOffs(68, 8).mirror().addBox(-22F, -2F, -2F, 24F, 4F, 4F, CubeDeformation(0.1F)).texOffs(56, 66).mirror().addBox(-22F, -5F, 0F, 24F, 3F, 0F).texOffs(68, 18).mirror().addBox(-22F, 2F, 0F, 24F, 1F, 0F).texOffs(0, 0).mirror().addBox(-22F, -2.5F, -2.5F, 7F, 6F, 5F, CubeDeformation(0.25F)).texOffs(0, 46).mirror().addBox(-26F, -3.5F, 0F, 4F, 8F, 0F), PartPose.ZERO).apply {
                        addOrReplaceChild("right_leg_front1", CubeListBuilder.create().texOffs(68, 0).mirror().addBox(-24F, -2F, -2F, 24F, 4F, 4F).texOffs(68, 16).mirror().addBox(-24F, -4F, -0.1F, 24F, 2F, 0F).texOffs(68, 19).mirror().addBox(-24F, 2F, 0F, 24F, 1F, 0F).texOffs(19, 0).mirror().addBox(-28F, -2F, 0F, 4F, 4F, 0F), PartPose.offset(-20F, 0F, 0F))
                    }
                }
                addOrReplaceChild("right_leg_joint2", CubeListBuilder.create(), PartPose.offset(-8F, -7F, -9F)).apply {
                    addOrReplaceChild("right_leg2", CubeListBuilder.create().texOffs(68, 8).mirror().addBox(-22F, -2F, -2F, 24F, 4F, 4F, CubeDeformation(0.1F)).texOffs(56, 66).mirror().addBox(-22F, -5F, 0F, 24F, 3F, 0F).texOffs(68, 18).mirror().addBox(-22F, 2F, 0F, 24F, 1F, 0F).texOffs(0, 0).mirror().addBox(-22F, -2.5F, -2.5F, 7F, 6F, 5F, CubeDeformation(0.25F)).texOffs(0, 46).mirror().addBox(-26F, -3.5F, -0.25F, 4F, 8F, 0F), PartPose.ZERO).apply {
                        addOrReplaceChild("right_leg_front2", CubeListBuilder.create().texOffs(68, 0).mirror().addBox(-24F, -2F, -2F, 24F, 4F, 4F).texOffs(68, 16).mirror().addBox(-24F, -4F, -0.1F, 24F, 2F, 0F).texOffs(68, 19).mirror().addBox(-24F, 2F, 0F, 24F, 1F, 0F).texOffs(19, 0).mirror().addBox(-28F, -2F, 0F, 4F, 4F, 0F), PartPose.offset(-20F, 0F, 0F))
                    }
                }
                addOrReplaceChild("right_leg_joint3", CubeListBuilder.create(), PartPose.offset(-8F, -9F, -3F)).apply {
                    addOrReplaceChild("right_leg3", CubeListBuilder.create().texOffs(68, 8).mirror().addBox(-22F, -2F, -2F, 24F, 4F, 4F, CubeDeformation(0.1F)).texOffs(56, 66).mirror().addBox(-22F, -5F, 0F, 24F, 3F, 0F).texOffs(68, 18).mirror().addBox(-22F, 2F, 0F, 24F, 1F, 0F).texOffs(0, 0).mirror().addBox(-22F, -2.5F, -2.5F, 7F, 6F, 5F, CubeDeformation(0.25F)).texOffs(0, 46).mirror().addBox(-26F, -3.5F, 0F, 4F, 8F, 0F), PartPose.ZERO).apply {
                        addOrReplaceChild("right_leg_front3", CubeListBuilder.create().texOffs(68, 0).mirror().addBox(-24F, -2F, -2F, 24F, 4F, 4F).texOffs(68, 16).mirror().addBox(-24F, -4F, -0.1F, 24F, 2F, 0F).texOffs(68, 19).mirror().addBox(-24F, 2F, 0F, 24F, 1F, 0F).texOffs(19, 0).mirror().addBox(-28F, -2F, 0F, 4F, 4F, 0F), PartPose.offset(-20F, 0F, 0F))
                    }
                }
            }
        }
        LayerDefinition.create(it, 128, 128)
    }
}