package com.conpresp.conprespapi.dto;

import com.conpresp.conprespapi.entity.GraphicDocumentation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Lob;
import java.util.Base64;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GraphicRequest {

    @Lob
    private String image;

    public GraphicDocumentation toGraphicDocumentation() {
        return new GraphicDocumentation(
                this.getImage()
        );
    }
}
